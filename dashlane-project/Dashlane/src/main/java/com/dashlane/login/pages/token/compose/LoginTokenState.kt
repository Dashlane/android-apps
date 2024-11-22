package com.dashlane.login.pages.token.compose

import com.dashlane.mvvm.State

sealed class LoginTokenState : State {
    data class View(
        val email: String,
        val token: String? = null,
        val isLoading: Boolean = false,
        val showHelpDialog: Boolean = false,
        val error: LoginTokenError? = null,
    ) : LoginTokenState(), State.View

    sealed class SideEffect : LoginTokenState(), State.SideEffect {
        data object Success : SideEffect()
    }
}

sealed class LoginTokenError : Exception() {
    data object InvalidToken : LoginTokenError()
    data object Network : LoginTokenError()
    data object Offline : LoginTokenError()
}