package com.dashlane.wear.login.remote.authorize

import com.dashlane.mvvm.State

sealed class WearAuthorizeState : State {
    sealed class View : WearAuthorizeState(), State.View {
        data object Initial : View()

        data object Success : View()

        data object Error : View()
    }

    sealed class SideEffect : WearAuthorizeState(), State.SideEffect {
        data class GoToFreeUserPaywall(val limit: Int) : SideEffect()

        data object OnSuccess : SideEffect()
    }
}