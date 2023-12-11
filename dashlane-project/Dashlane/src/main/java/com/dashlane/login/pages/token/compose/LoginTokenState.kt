package com.dashlane.login.pages.token.compose

import com.dashlane.authentication.RegisteredUserDevice

sealed class LoginTokenState {
    abstract val data: LoginTokenData

    data class Initial(override val data: LoginTokenData) : LoginTokenState()
    data class DebugToken(override val data: LoginTokenData) : LoginTokenState()
    data class Loading(override val data: LoginTokenData) : LoginTokenState()
    data class Success(
        override val data: LoginTokenData,
        val registeredUserDevice: RegisteredUserDevice.Remote,
        val authTicket: String
    ) : LoginTokenState()

    data class Error(override val data: LoginTokenData, val error: LoginTokenError) : LoginTokenState()
}

data class LoginTokenData(
    val email: String? = null,
    val token: String? = null
)

sealed class LoginTokenError : Exception() {
    object InvalidToken : LoginTokenError()
    object Network : LoginTokenError()
    object Offline : LoginTokenError()
}