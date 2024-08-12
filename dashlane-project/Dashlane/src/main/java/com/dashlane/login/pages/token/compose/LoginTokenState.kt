package com.dashlane.login.pages.token.compose

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.mvvm.State

data class LoginTokenState(
    val email: String,
    val token: String? = null,
    val isLoading: Boolean = false,
    val showHelpDialog: Boolean = false,
    val error: LoginTokenError? = null,
) : State

sealed class LoginTokenNavigationState : State {
    data class Success(val registeredUserDevice: RegisteredUserDevice.Remote, val authTicket: String) : LoginTokenNavigationState()
}

sealed class LoginTokenError : Exception() {
    data object InvalidToken : LoginTokenError()
    data object Network : LoginTokenError()
    data object Offline : LoginTokenError()
}