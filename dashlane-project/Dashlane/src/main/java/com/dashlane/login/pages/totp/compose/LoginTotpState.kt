package com.dashlane.login.pages.totp.compose

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.mvvm.State

data class LoginTotpState(
    val email: String,
    val verificationMode: VerificationMode? = null,
    val otp: String? = null,
    val isLoading: Boolean = false,
    val isAuthenticatorEnabled: Boolean = false,
    val error: LoginTotpError? = null,
    val isRecoveryError: Boolean = false,
    val showHelpDialog: Boolean = false,
    val showRecoveryCodeDialog: Boolean = false,
    val showSendTextMessageDialog: Boolean = false,
    val showTextMessageDialog: Boolean = false
) : State

sealed class LoginTotpNavigationState : State {
    data class GoToPush(val email: String) : LoginTotpNavigationState()
    data class Success(val registeredUserDevice: RegisteredUserDevice, val authTicket: String) : LoginTotpNavigationState()
}

sealed class LoginTotpError : Exception() {
    data object InvalidTokenLockedOut : LoginTotpError()
    data object InvalidToken : LoginTotpError()
    data object AlreadyUsed : LoginTotpError()
    data object Network : LoginTotpError()
    data object Offline : LoginTotpError()
}