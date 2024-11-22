package com.dashlane.login.pages.pin.compose

import android.content.Intent
import com.dashlane.lock.LockSetting
import com.dashlane.mvvm.State

data class LoginPinState(
    val email: String,
    val pinCode: String? = null,
    val pinLength: Int? = null,
    val lockSetting: LockSetting? = null,
    val fallback: LoginPinFallback = LoginPinFallback.MP,
    val isMPLess: Boolean = false,
    val error: LoginPinError? = null,
    val isSystemLockSetup: Boolean = true,
    val helpDialogShown: Boolean = false,
)

sealed class LoginPinNavigationState : State {
    data object UnlockSuccess : LoginPinNavigationState()
    data class Cancel(val fallback: LoginPinFallback) : LoginPinNavigationState()
    data class GoToRecoveryHelp(val email: String) : LoginPinNavigationState()
    data class GoToSecretTransfer(val email: String) : LoginPinNavigationState()
    data class Logout(val email: String, val isMPLess: Boolean, val errorMessage: String? = null) : LoginPinNavigationState()
    data class GoToSystemLockSetting(val intent: Intent) : LoginPinNavigationState()
}

sealed class LoginPinFallback {
    data object SSO : LoginPinFallback()
    data object MPLess : LoginPinFallback()
    data object MP : LoginPinFallback()
    data object Cancellable : LoginPinFallback()
}

sealed class LoginPinError {
    data class WrongPin(val attempt: Int) : LoginPinError()
}