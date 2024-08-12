package com.dashlane.login.pages.biometric.compose

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.user.UserAccountInfo
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.login.lock.LockManager
import com.dashlane.login.lock.LockPass
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.user.Username
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

@HiltViewModel
class LoginBiometricViewModel @Inject constructor(
    private val biometricAuthModule: BiometricAuthModule,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val userPreferencesManager: UserPreferencesManager,
    private val lockManager: LockManager,
    private val loginLogger: LoginLogger,
) : ViewModel() {

    private val stateFlow = MutableStateFlow(LoginBiometricState())
    private val navigationStateFlow = MutableSharedFlow<LoginBiometricNavigationState>()

    val uiState = stateFlow.asStateFlow()
    val navigationState = navigationStateFlow.asSharedFlow()

    fun viewStarted(userAccountInfo: UserAccountInfo, lockSetting: LockSetting) {
        viewModelScope.launch {
            val email = userAccountInfo.username
            val biometricActivationStatus = biometricAuthModule.getBiometricActivationStatus()
            val allowedAuthenticator = biometricAuthModule.getPromptAuthenticator(biometricActivationStatus)
            val biometricStatus = biometricAuthModule.checkBiometricStatus(email, biometricActivationStatus)
            val fallback = when {
                lockSetting.isLockCancelable -> LoginBiometricFallback.Cancellable
                userAccountInfo.sso -> LoginBiometricFallback.SSO
                userAccountInfo.accountType is UserAccountInfo.AccountType.InvisibleMasterPassword -> LoginBiometricFallback.MPLess
                else -> LoginBiometricFallback.MP
            }

            when (biometricStatus) {
                is BiometricAuthModule.Result.BiometricEnrolled -> {
                    stateFlow.update { state ->
                        state.copy(
                            isBiometricPromptDisplayed = true,
                            allowedAuthenticator = allowedAuthenticator,
                            fallback = fallback,
                            cryptoObject = biometricStatus.cipher?.let { CryptoObject(biometricStatus.cipher) },
                            email = email,
                            lockSetting = lockSetting
                        )
                    }
                }
                is BiometricAuthModule.Result.SecurityHasChanged -> {
                    securityHasChanged()
                    navigationStateFlow.emit(LoginBiometricNavigationState.Logout(stateFlow.value.email, fallback))
                }
                else -> stateFlow.update { state -> state.copy(error = LoginBiometricError.Generic()) }
            }
        }
    }

    fun authenticationError(errorCode: Int, errorMessage: String) {
        viewModelScope.launch {
            val fallback = stateFlow.value.fallback
            val navigationState = when (errorCode) {
                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                    if (stateFlow.value.lockSetting?.isLockCancelable == true) {
                        LoginBiometricNavigationState.Cancel
                    } else {
                        LoginBiometricNavigationState.Fallback(fallback)
                    }
                }
                BiometricPrompt.ERROR_USER_CANCELED,
                BiometricPrompt.ERROR_TIMEOUT -> LoginBiometricNavigationState.Cancel
                BiometricPrompt.ERROR_LOCKOUT -> LoginBiometricNavigationState.Lockout(fallback)
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> LoginBiometricNavigationState.Logout(stateFlow.value.email, fallback)
                BiometricPrompt.ERROR_CANCELED -> {
                    
                    return@launch
                }

                BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                    
                    securityHasChanged()
                    LoginBiometricNavigationState.Logout(stateFlow.value.email, fallback, error = LoginBiometricError.Generic(errorMessage))
                }
                else -> LoginBiometricNavigationState.Lockout(fallback, error = LoginBiometricError.Generic(errorMessage))
            }
            loginLogger.logErrorUnknown(loginMode = LoginMode.Biometric)
            stateFlow.update { state -> state.copy(isBiometricPromptDisplayed = false) }
            navigationStateFlow.emit(navigationState)
        }
    }

    fun authenticationSuccess(cryptoObject: CryptoObject?) {
        viewModelScope.launch {
            val unlock = when {
                stateFlow.value.cryptoObject != null && cryptoObject != null -> lockManager.unlock(LockPass.ofBiometric(cryptoObject))
                stateFlow.value.cryptoObject == null && cryptoObject == null -> lockManager.unlock(LockPass.ofWeakBiometric())
                else -> false
            }

            when (unlock) {
                true -> {
                    loginLogger.logSuccess(loginMode = LoginMode.Biometric)
                    stateFlow.value.lockSetting?.unlockReason?.let { reason ->
                        if (reason is UnlockEvent.Reason.WithCode && reason.origin == UnlockEvent.Reason.WithCode.Origin.CHANGE_MASTER_PASSWORD) {
                            return@let
                        }
                        runCatching { lockManager.sendUnLock(reason, true) }
                    }
                    stateFlow.update { state -> state.copy(isBiometricPromptDisplayed = false) }
                    navigationStateFlow.emit(LoginBiometricNavigationState.UnlockSuccess)
                }
                false -> authenticationFailed()
            }
        }
    }

    fun authenticationFailed() {
        viewModelScope.launch {
            loginLogger.logWrongBiometric()
            lockManager.addFailUnlockAttempt()
            if (!lockManager.hasFailedUnlockTooManyTimes()) {
                
                return@launch
            }
            stateFlow.update { state -> state.copy(isBiometricPromptDisplayed = false) }
            when (stateFlow.value.fallback) {
                is LoginBiometricFallback.MPLess -> {
                    navigationStateFlow.emit(
                        LoginBiometricNavigationState.Lockout(
                            fallback = stateFlow.value.fallback,
                            error = LoginBiometricError.TooManyAttempt
                        )
                    )
                }
                else -> {
                    navigationStateFlow.emit(
                        LoginBiometricNavigationState.Logout(
                            email = stateFlow.value.email,
                            fallback = stateFlow.value.fallback,
                            error = LoginBiometricError.TooManyAttempt
                        )
                    )
                }
            }
        }
    }

    @VisibleForTesting
    fun securityHasChanged() {
        when (stateFlow.value.fallback) {
            is LoginBiometricFallback.MPLess -> lockManager.setLockType(LockTypeManager.LOCK_TYPE_PIN_CODE)
            else -> {
                lockManager.setLockType(LockTypeManager.LOCK_TYPE_MASTER_PASSWORD)
                stateFlow.value.email?.let { sessionCredentialsSaver.deleteSavedCredentials(Username.ofEmail(it)) }
            }
        }
        userPreferencesManager.putBoolean(ConstantsPrefs.INVALIDATED_BIOMETRIC, true)
    }
}