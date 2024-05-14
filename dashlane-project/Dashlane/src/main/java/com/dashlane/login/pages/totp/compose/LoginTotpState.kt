package com.dashlane.login.pages.totp.compose

import com.dashlane.authentication.RegisteredUserDevice

sealed class LoginTotpState {
    abstract val data: LoginTotpData

    data class Initial(override val data: LoginTotpData) : LoginTotpState()
    data class Loading(override val data: LoginTotpData) : LoginTotpState()
    data class Success(
        override val data: LoginTotpData,
        val registeredUserDevice: RegisteredUserDevice,
        val authTicket: String
    ) : LoginTotpState()

    data class Error(override val data: LoginTotpData, val error: LoginTotpError) : LoginTotpState()
}

data class LoginTotpData(
    val email: String? = null,
    val otp: String? = null,
    val recoveryToken: String? = null,
    val showHelpDialog: Boolean = false,
    val showRecoveryCodeDialog: Boolean = false,
    val showSendTextMessageDialog: Boolean = false,
    val showTextMessageDialog: Boolean = false
)

sealed class LoginTotpError : Exception() {
    data object InvalidToken : LoginTotpError()
    data object AlreadyUsed : LoginTotpError()
    data object Network : LoginTotpError()
    data object Offline : LoginTotpError()
}