package com.dashlane.login.root

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.authentication.toSecurityFeatures
import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.data.login.SsoInfo
import com.dashlane.data.login.toMigrationToSsoMemberInfo
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPrompt
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockSetting.Companion.buildFrom
import com.dashlane.lock.LockType
import com.dashlane.lock.LockTypeManager
import com.dashlane.login.LocalLoginDestination
import com.dashlane.login.LoginActivity.Companion.ALLOW_SKIP_EMAIL
import com.dashlane.login.LoginActivity.Companion.LOCK_SETTING
import com.dashlane.login.LoginRepository
import com.dashlane.login.LoginStrategy
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.navigation.NavigationConstants
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.LocalKeyRepository
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val lockManager: LockManager,
    private val loginRepository: LoginRepository,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val lockTypeManager: LockTypeManager,
    private val localKeyRepository: LocalKeyRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _stateFlow: MutableViewStateFlow<LoginState.View, LoginState.SideEffect>
    val stateFlow: ViewStateFlow<LoginState.View, LoginState.SideEffect>

    init {
        val lockSetting: LockSetting = savedStateHandle[LOCK_SETTING] ?: buildFrom(savedStateHandle)
        val leaveAfterSuccess = savedStateHandle[NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN] ?: false
        val allowSkipEmail = savedStateHandle[ALLOW_SKIP_EMAIL] ?: false
        _stateFlow = MutableViewStateFlow(
            LoginState.View.Initial(
                lockSetting = lockSetting,
                leaveAfterSuccess = leaveAfterSuccess,
                allowSkipEmail = allowSkipEmail,
            ),
        )
        stateFlow = _stateFlow
    }

    @Suppress("ComplexCondition")
    fun viewStarted() {
        viewModelScope.launch {
            val state = _stateFlow.value
            if (state !is LoginState.View.Initial) return@launch

            val lockSetting = _stateFlow.value.lockSetting
            val leaveAfterSuccess = _stateFlow.value.leaveAfterSuccess
            val defaultUsername = globalPreferencesManager.getDefaultUsername()
            val currentUser: UserAccountInfo? = defaultUsername?.let { userAccountStorage[it] }
            val localKeyCreated = defaultUsername?.let { localKeyRepository.isLocalKeyCreated(it) } ?: false

            val isSSO = sessionManager.session == null && currentUser?.sso == true

            if (currentUser == null || isSSO || globalPreferencesManager.isUserLoggedOut || !localKeyCreated) {
                _stateFlow.update {
                    LoginState.View.Remote(
                        email = currentUser?.username,
                        allowSkipEmail = state.allowSkipEmail,
                        lockSetting = lockSetting,
                        leaveAfterSuccess = leaveAfterSuccess,
                    )
                }
            } else {
                val locks = lockTypeManager.getLocks(Username.ofEmail(currentUser.username))
                localLogin(
                    userAccountInfo = currentUser,
                    lockSetting = lockSetting.copy(locks = locks),
                    leaveAfterSuccess = leaveAfterSuccess,
                )
            }
        }
    }

    fun showSnackBar(message: String) {
        viewModelScope.launch {
            _stateFlow.send(LoginState.SideEffect.ShowSnackBar(message))
        }
    }

    fun createAccount(email: String, skipEmailIfPrefilled: Boolean) {
        viewModelScope.launch {
            _stateFlow.send(LoginState.SideEffect.RemoteSuccess.CreateAccount(email, skipEmailIfPrefilled))
        }
    }

    fun endOfLife() {
        viewModelScope.launch {
            _stateFlow.send(LoginState.SideEffect.RemoteSuccess.EndOfLife)
        }
    }

    fun remoteLoginSuccess(strategy: LoginStrategy.Strategy?, ssoInfo: SsoInfo?) {
        viewModelScope.launch {
            val lockSetting = _stateFlow.value.lockSetting

            if (_stateFlow.value.leaveAfterSuccess == true) {
                _stateFlow.send(LoginState.SideEffect.RemoteSuccess.FinishWithSuccess)
            } else {
                val username = sessionManager.session?.userId
                val ukiRequiresMonobucketConfirmation: Boolean =
                    username?.let { preferencesManager[username].ukiRequiresMonobucketConfirmation } ?: false
                _stateFlow.send(
                    LoginState.SideEffect.RemoteSuccess.Finish(
                        email = username,
                        strategy = strategy,
                        lockSetting = lockSetting,
                        migrationToSsoMemberInfo = ssoInfo?.toMigrationToSsoMemberInfo(),
                        ukiRequiresMonobucketConfirmation = ukiRequiresMonobucketConfirmation,
                    ),
                )
            }
        }
    }

    fun localLoginSuccess(strategy: LoginStrategy.Strategy?) {
        viewModelScope.launch {
            val username = sessionManager.session?.userId
            val ukiRequiresMonobucketConfirmation: Boolean =
                username?.let { preferencesManager[username].ukiRequiresMonobucketConfirmation } ?: false
            _stateFlow.send(
                LoginState.SideEffect.LocalSuccess.Success(
                    email = username,
                    strategy = strategy,
                    lockSetting = _stateFlow.value.lockSetting,
                    migrationToSsoMemberInfo = null,
                    ukiRequiresMonobucketConfirmation = ukiRequiresMonobucketConfirmation,
                ),
            )
        }
    }

    fun cancel() {
        viewModelScope.launch {
            lockManager.sendLockCancelled()
            _stateFlow.send(LoginState.SideEffect.LocalSuccess.Cancel(_stateFlow.value.lockSetting))
        }
    }

    fun changeAccount(email: String?) {
        viewModelScope.launch {
            _stateFlow.update {
                LoginState.View.Remote(
                    email = email,
                    allowSkipEmail = true,
                    lockSetting = _stateFlow.value.lockSetting.copy(redirectToHome = true),
                    leaveAfterSuccess = _stateFlow.value.leaveAfterSuccess,
                )
            }
        }
    }

    fun logout(email: String?, isMPLess: Boolean = false) {
        viewModelScope.launch {
            sessionManager.session?.let { sessionManager.destroySession(it, true) }
            _stateFlow.update {
                LoginState.View.Remote(
                    email = email,
                    allowSkipEmail = isMPLess,
                    lockSetting = _stateFlow.value.lockSetting.copy(redirectToHome = true),
                    leaveAfterSuccess = _stateFlow.value.leaveAfterSuccess,
                )
            }
        }
    }

    fun goToArk(registeredUserDevice: RegisteredUserDevice?, accountType: UserAccountInfo.AccountType) {
        viewModelScope.launch {
            lockManager.sendLockCancelled()
            val registeredUserDevice = registeredUserDevice ?: loginRepository.getRegisteredUserDevice() ?: return@launch
            val authTicket = loginRepository.getAuthTicket()
            _stateFlow.send(LoginState.SideEffect.GoToARK(registeredUserDevice, accountType, authTicket))
        }
    }

    @VisibleForTesting
    fun localLogin(userAccountInfo: UserAccountInfo, lockSetting: LockSetting, leaveAfterSuccess: Boolean) {
        viewModelScope.launch {
            val userPreferencesManager = preferencesManager[userAccountInfo.username]
            val shouldShowPasswordForRemember = when {
                userAccountInfo.accountType is UserAccountInfo.AccountType.InvisibleMasterPassword -> false
                userAccountInfo.sso -> false
                LockType.PinCode !in lockSetting.locks && LockType.Biometric !in lockSetting.locks -> false
                else -> userPreferencesManager.credentialsSaveDate.plus(Duration.ofDays(14L)) < Instant.now()
            }

            val isUserLoggedIn = sessionManager.session != null

            val forceMasterPassword = if ((lockSetting.lockPrompt as? LockPrompt.ForSettings)?.forceMasterPassword == true) {
                !userAccountInfo.sso && userAccountInfo.accountType !is UserAccountInfo.AccountType.InvisibleMasterPassword
            } else {
                false
            }

            val lockSetting = lockSetting.copy(isShowMPForRemember = shouldShowPasswordForRemember, isLoggedIn = isUserLoggedIn)

            val showOtpScreen =
                
                (userAccountInfo.otp2 && !isUserLoggedIn) ||
                    
                    ((userAccountInfo.securitySettings?.isTotp == true || userAccountInfo.otp2) && lockSetting.isMasterPasswordReset)

            val showPinLock = LockType.PinCode in lockSetting.locks && !shouldShowPasswordForRemember && isUserLoggedIn
            val showBiometricLock = LockType.Biometric in lockSetting.locks && !shouldShowPasswordForRemember && isUserLoggedIn

            val startDestination = when {
                showOtpScreen -> LocalLoginDestination.Otp2(userAccountInfo.username)
                forceMasterPassword -> LocalLoginDestination.Password
                showBiometricLock -> LocalLoginDestination.Biometric(userAccountInfo.username)
                showPinLock -> LocalLoginDestination.Pin(userAccountInfo.username)
                userAccountInfo.sso -> LocalLoginDestination.Sso
                else -> {
                    when (userAccountInfo.accountType) {
                        UserAccountInfo.AccountType.MasterPassword -> LocalLoginDestination.Password
                        UserAccountInfo.AccountType.InvisibleMasterPassword -> LocalLoginDestination.SecretTransfer(
                            userAccountInfo.username,
                        )
                    }
                }
            }

            val registeredUserDevice = RegisteredUserDevice.Local(
                login = userAccountInfo.username,
                securityFeatures = userAccountInfo.securitySettings.toSecurityFeatures(),
                accessKey = userAccountInfo.accessKey,
            )

            loginRepository.updateRegisteredUserDevice(registeredUserDevice)

            _stateFlow.update {
                LoginState.View.Local(
                    lockSetting = lockSetting,
                    leaveAfterSuccess = leaveAfterSuccess,
                    userAccountInfo = userAccountInfo,
                    startDestination = startDestination,
                )
            }
        }
    }
}