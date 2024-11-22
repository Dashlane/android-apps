package com.dashlane.login.pages.biometric.compose

import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.hardwaresecurity.BiometricAuthModule
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.lock.LockPass
import com.dashlane.lock.LockSetting
import com.dashlane.lock.LockType
import com.dashlane.login.LoginLogger
import com.dashlane.login.LoginMode
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.user.UserAccountInfo
import com.dashlane.user.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting
import javax.inject.Inject

@HiltViewModel
class LoginBiometricViewModel @Inject constructor(
    private val biometricAuthModule: BiometricAuthModule,
    private val sessionManager: SessionManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val preferencesManager: PreferencesManager,
    private val lockManager: LockManager,
    private val loginLogger: LoginLogger,
) : ViewModel() {

    private val _stateFlow = MutableViewStateFlow<LoginBiometricState.View, LoginBiometricState.SideEffect>(LoginBiometricState.View())
    val stateFlow: ViewStateFlow<LoginBiometricState.View, LoginBiometricState.SideEffect> = _stateFlow

    fun viewStarted(userAccountInfo: UserAccountInfo, lockSetting: LockSetting, isBiometricRecovery: Boolean) {
        viewModelScope.launch {
            if (_stateFlow.value.isBiometricPromptDisplayed) return@launch

            val email = userAccountInfo.username
            val biometricActivationStatus = biometricAuthModule.getBiometricActivationStatus()
            val allowedAuthenticator = biometricAuthModule.getPromptAuthenticator(biometricActivationStatus)
            val biometricStatus = biometricAuthModule.checkBiometricStatus(email, biometricActivationStatus)
            val locks = lockManager.getLocks(Username.ofEmail(email))
            val fallback = when {
                isBiometricRecovery -> LoginBiometricFallback.Cancellable
                userAccountInfo.sso -> LoginBiometricFallback.SSO
                LockType.PinCode in locks -> LoginBiometricFallback.Pin
                else -> LoginBiometricFallback.Password
            }

            when (biometricStatus) {
                is BiometricAuthModule.Result.BiometricEnrolled -> {
                    _stateFlow.update { state ->
                        state.copy(
                            isBiometricPromptDisplayed = true,
                            allowedAuthenticator = allowedAuthenticator,
                            fallback = fallback,
                            cryptoObject = biometricStatus.cipher?.let { cipher -> CryptoObject(cipher) },
                            email = email,
                            lockSetting = lockSetting
                        )
                    }
                }
                is BiometricAuthModule.Result.SecurityHasChanged -> {
                    securityHasChanged()
                    _stateFlow.send(LoginBiometricState.SideEffect.Logout(_stateFlow.value.email, fallback))
                }
                else -> _stateFlow.update { state -> state.copy(error = LoginBiometricError.Generic()) }
            }
        }
    }

    fun authenticationError(errorCode: Int, errorMessage: String) {
        viewModelScope.launch {
            val fallback = _stateFlow.value.fallback
            val navigationState = when (errorCode) {
                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> LoginBiometricState.SideEffect.Fallback(fallback)
                BiometricPrompt.ERROR_USER_CANCELED,
                BiometricPrompt.ERROR_TIMEOUT -> LoginBiometricState.SideEffect.Cancel
                BiometricPrompt.ERROR_LOCKOUT -> LoginBiometricState.SideEffect.Lockout(fallback, error = LoginBiometricError.Generic(errorMessage))
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> LoginBiometricState.SideEffect.Logout(
                    email = _stateFlow.value.email,
                    fallback = fallback,
                    error = LoginBiometricError.Generic(errorMessage)
                )
                BiometricPrompt.ERROR_CANCELED -> {
                    
                    return@launch
                }

                BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                    
                    securityHasChanged()
                    LoginBiometricState.SideEffect.Logout(_stateFlow.value.email, fallback, error = LoginBiometricError.Generic(errorMessage))
                }
                else -> LoginBiometricState.SideEffect.Lockout(fallback, error = LoginBiometricError.Generic(errorMessage))
            }
            loginLogger.logErrorUnknown(loginMode = LoginMode.Biometric)
            _stateFlow.update { state -> state.copy(isBiometricPromptDisplayed = false) }
            _stateFlow.send(navigationState)
        }
    }

    fun authenticationSuccess(cryptoObject: CryptoObject?) {
        viewModelScope.launch {
            val session = sessionManager.session ?: run {
                _stateFlow.send(LoginBiometricState.SideEffect.Logout(email = null, _stateFlow.value.fallback, error = null))
                return@launch
            }
            val unlock = when {
                _stateFlow.value.cryptoObject != null && cryptoObject != null -> lockManager.unlock(session, LockPass.ofBiometric(cryptoObject))
                _stateFlow.value.cryptoObject == null && cryptoObject == null -> lockManager.unlock(session, LockPass.ofWeakBiometric())
                else -> false
            }

            when (unlock) {
                true -> {
                    loginLogger.logSuccess(loginMode = LoginMode.Biometric)
                    val reason = _stateFlow.value.lockSetting?.unlockReason ?: LockEvent.Unlock.Reason.AppAccess
                    if (reason !is LockEvent.Unlock.Reason.WithCode || reason.origin != LockEvent.Unlock.Reason.WithCode.Origin.CHANGE_MASTER_PASSWORD) {
                        runCatching { lockManager.sendUnlockEvent(LockEvent.Unlock(reason = reason, lockType = LockType.Biometric)) }
                    }
                    _stateFlow.update { state -> state.copy(isBiometricPromptDisplayed = false) }
                    _stateFlow.send(LoginBiometricState.SideEffect.UnlockSuccess)
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
            _stateFlow.update { state -> state.copy(isBiometricPromptDisplayed = false) }
            when (_stateFlow.value.fallback) {
                is LoginBiometricFallback.Pin -> {
                    _stateFlow.send(
                        LoginBiometricState.SideEffect.Lockout(
                            fallback = _stateFlow.value.fallback,
                            error = LoginBiometricError.TooManyAttempt
                        )
                    )
                }
                else -> {
                    _stateFlow.send(
                        LoginBiometricState.SideEffect.Logout(
                            email = _stateFlow.value.email,
                            fallback = _stateFlow.value.fallback,
                            error = LoginBiometricError.TooManyAttempt
                        )
                    )
                }
            }
        }
    }

    @VisibleForTesting
    fun securityHasChanged() {
        _stateFlow.value.email?.let { email ->
            val username = Username.ofEmail(email)
            lockManager.removeLock(username, LockType.Biometric)
            if (_stateFlow.value.fallback !is LoginBiometricFallback.Pin) {
                sessionCredentialsSaver.deleteSavedCredentials(username)
            }
            preferencesManager[username].putBoolean(ConstantsPrefs.INVALIDATED_BIOMETRIC, true)
        }
    }
}