package com.dashlane.login.pages.biometric.compose

import androidx.biometric.BiometricPrompt
import com.dashlane.lock.LockSetting
import com.dashlane.mvvm.State

sealed class LoginBiometricState : State {
    data class View(
        val isBiometricPromptDisplayed: Boolean = false,
        val allowedAuthenticator: Int = -1,
        val fallback: LoginBiometricFallback = LoginBiometricFallback.Password,
        val cryptoObject: BiometricPrompt.CryptoObject? = null,
        val email: String? = null,
        val isRecovery: Boolean = false,
        val lockSetting: LockSetting? = null,
        val error: LoginBiometricError? = null
    ) : LoginBiometricState(), State.View

    sealed class SideEffect : LoginBiometricState(), State.SideEffect {
        data object UnlockSuccess : SideEffect()
        data object Cancel : SideEffect()
        data class Fallback(val fallback: LoginBiometricFallback) : SideEffect()
        data class Lockout(val fallback: LoginBiometricFallback, val error: LoginBiometricError? = null) : SideEffect()
        data class Logout(val email: String?, val fallback: LoginBiometricFallback, val error: LoginBiometricError? = null) : SideEffect()
    }
}

sealed class LoginBiometricFallback {
    data object Cancellable : LoginBiometricFallback()
    data object SSO : LoginBiometricFallback()
    data object Pin : LoginBiometricFallback()
    data object Password : LoginBiometricFallback()
}

sealed class LoginBiometricError {
    data class Generic(val errorMessage: String? = null) : LoginBiometricError()
    data object TooManyAttempt : LoginBiometricError()
}