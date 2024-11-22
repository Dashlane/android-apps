package com.dashlane.login.pages.totp.compose

import com.dashlane.hermes.generated.definitions.VerificationMode
import com.dashlane.lock.LockType
import com.dashlane.mvvm.State

sealed class LoginTotpState : State {
    data class View(
        val email: String,
        val restoreSession: Boolean,
        val verificationMode: VerificationMode? = null,
        val otp: String? = null,
        val isLoading: Boolean = false,
        val error: LoginTotpError? = null,
        val isRecoveryError: Boolean = false,
        val showHelpDialog: Boolean = false,
        val showRecoveryCodeDialog: Boolean = false,
        val showSendTextMessageDialog: Boolean = false,
        val showTextMessageDialog: Boolean = false
    ) : LoginTotpState(), State.View

    sealed class SideEffect : LoginTotpState(), State.SideEffect {
        data class Success(val locks: List<LockType>) : SideEffect()
    }
}

sealed class LoginTotpError : Exception() {
    data object InvalidTokenLockedOut : LoginTotpError()
    data object InvalidToken : LoginTotpError()
    data object AlreadyUsed : LoginTotpError()
    data object Network : LoginTotpError()
    data object Offline : LoginTotpError()
}