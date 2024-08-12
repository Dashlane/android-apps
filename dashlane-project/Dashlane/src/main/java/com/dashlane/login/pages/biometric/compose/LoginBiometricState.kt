package com.dashlane.login.pages.biometric.compose

import androidx.biometric.BiometricPrompt
import com.dashlane.login.lock.LockSetting
import com.dashlane.mvvm.State

data class LoginBiometricState(
    val isBiometricPromptDisplayed: Boolean = false,
    val allowedAuthenticator: Int = -1,
    val fallback: LoginBiometricFallback = LoginBiometricFallback.MP,
    val cryptoObject: BiometricPrompt.CryptoObject? = null,
    val email: String? = null,
    val isRecovery: Boolean = false,
    val lockSetting: LockSetting? = null,
    val error: LoginBiometricError? = null
)

sealed class LoginBiometricNavigationState : State {
    data object UnlockSuccess : LoginBiometricNavigationState()
    data object Cancel : LoginBiometricNavigationState()
    data class Fallback(val fallback: LoginBiometricFallback) : LoginBiometricNavigationState()
    data class Lockout(val fallback: LoginBiometricFallback, val error: LoginBiometricError? = null) : LoginBiometricNavigationState()
    data class Logout(val email: String?, val fallback: LoginBiometricFallback, val error: LoginBiometricError? = null) : LoginBiometricNavigationState()
}

sealed class LoginBiometricFallback {
    data object SSO : LoginBiometricFallback()
    data object MPLess : LoginBiometricFallback()
    data object MP : LoginBiometricFallback()
    data object Cancellable : LoginBiometricFallback()
}

sealed class LoginBiometricError {
    data class Generic(val errorMessage: String? = null) : LoginBiometricError()
    data object TooManyAttempt : LoginBiometricError()
}