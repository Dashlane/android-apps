package com.dashlane.login.root

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.authentication.login.SsoInfo
import com.dashlane.authentication.toSecurityFeatures
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPrompt
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockType
import com.dashlane.lock.LockTypeManager
import com.dashlane.login.LoginStrategy
import com.dashlane.login.sso.toMigrationToSsoMemberInfo
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val lockManager: LockManager,
    private val loginRepository: LoginRepository,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val lockTypeManager: LockTypeManager,
) : ViewModel() {

    private val _stateFlow = MutableViewStateFlow<LoginState.View, LoginState.SideEffect>(LoginState.View.Initial())
    val stateFlow: ViewStateFlow<LoginState.View, LoginState.SideEffect> = _stateFlow

    fun viewStarted(lockSetting: LockSetting, allowSkipEmail: Boolean, leaveAfterSuccess: Boolean) {
        viewModelScope.launch {
            if (_stateFlow.value !is LoginState.View.Initial) return@launch

            val currentUser: UserAccountInfo? = globalPreferencesManager.getDefaultUsername()?.let { userAccountStorage[it] }
            val isSSO = sessionManager.session == null && currentUser?.sso == true

            if (currentUser == null || isSSO || globalPreferencesManager.isUserLoggedOut) {
                _stateFlow.update {
                    LoginState.View.Remote(
                        email = currentUser?.username,
                        allowSkipEmail = allowSkipEmail,
                        lockSetting = lockSetting,
                        leaveAfterSuccess = leaveAfterSuccess
                    )
                }
            } else {
                val locks = lockTypeManager.getLocks(Username.ofEmail(currentUser.username))
                localLogin(
                    userAccountInfo = currentUser,
                    lockSetting = lockSetting.copy(locks = locks),
                    leaveAfterSuccess = leaveAfterSuccess
                )
            }
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
                _stateFlow.send(LoginState.SideEffect.RemoteSuccess.FinishWithSuccess(lockSetting?.unlockReason))
            } else {
                val username = sessionManager.session?.userId
                val ukiRequiresMonobucketConfirmation: Boolean =
                    username?.let { preferencesManager[username].ukiRequiresMonobucketConfirmation } ?: false
                _stateFlow.send(
                    LoginState.SideEffect.RemoteSuccess.Finish(
                        strategy = strategy,
                        lockSetting = lockSetting,
                        migrationToSsoMemberInfo = ssoInfo?.toMigrationToSsoMemberInfo(),
                        ukiRequiresMonobucketConfirmation = ukiRequiresMonobucketConfirmation,
                    )
                )
            }
        }
    }

    fun localLoginSuccess(strategy: LoginStrategy.Strategy?) {
        viewModelScope.launch {
            val username = sessionManager.session?.userId
            val ukiRequiresMonobucketConfirmation: Boolean = username?.let { preferencesManager[username].ukiRequiresMonobucketConfirmation } ?: false
            _stateFlow.send(
                LoginState.SideEffect.LocalSuccess.Success(
                    strategy = strategy,
                    lockSetting = _stateFlow.value.lockSetting,
                    migrationToSsoMemberInfo = null,
                    ukiRequiresMonobucketConfirmation = ukiRequiresMonobucketConfirmation,
                )
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
            _stateFlow.send(LoginState.SideEffect.LocalSuccess.ChangeAccount(email, _stateFlow.value.lockSetting))
        }
    }

    fun logout(email: String?, isMPLess: Boolean = false) {
        viewModelScope.launch {
            sessionManager.session?.let { sessionManager.destroySession(it, true) }
            _stateFlow.send(LoginState.SideEffect.LocalSuccess.Logout(email, isMPLess))
        }
    }

    @VisibleForTesting
    suspend fun localLogin(userAccountInfo: UserAccountInfo, lockSetting: LockSetting, leaveAfterSuccess: Boolean) {
        viewModelScope.launch {
            val userPreferencesManager = preferencesManager[userAccountInfo.username]
            val shouldShowPasswordForRemember = when {
                userAccountInfo.accountType is UserAccountInfo.AccountType.InvisibleMasterPassword -> false
                userAccountInfo.sso -> false
                LockType.PinCode !in lockSetting.locks && LockType.Biometric !in lockSetting.locks -> false
                else -> userPreferencesManager.credentialsSaveDate.plus(Duration.ofDays(14L)) < Instant.now()
            }

            val isUserLoggedIn = sessionManager.session != null

            val forceMasterPassword = if (lockSetting.lockPrompt is LockPrompt.ForSettings) {
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
                showBiometricLock -> LocalLoginDestination.Biometric
                showPinLock -> LocalLoginDestination.Pin(userAccountInfo.username)
                userAccountInfo.sso -> LocalLoginDestination.Sso
                else -> {
                    when (userAccountInfo.accountType) {
                        UserAccountInfo.AccountType.MasterPassword -> LocalLoginDestination.Password
                        UserAccountInfo.AccountType.InvisibleMasterPassword -> LocalLoginDestination.SecretTransfer(userAccountInfo.username)
                    }
                }
            }

            val registeredUserDevice = RegisteredUserDevice.Local(
                login = userAccountInfo.username,
                securityFeatures = userAccountInfo.securitySettings.toSecurityFeatures(),
                accessKey = userAccountInfo.accessKey
            )

            loginRepository.updateRegisteredUserDevice(registeredUserDevice)

            _stateFlow.update {
                LoginState.View.Local(
                    lockSetting = lockSetting,
                    leaveAfterSuccess = leaveAfterSuccess,
                    registeredUserDevice = registeredUserDevice,
                    userAccountInfo = userAccountInfo,
                    startDestination = startDestination
                )
            }
        }
    }
}