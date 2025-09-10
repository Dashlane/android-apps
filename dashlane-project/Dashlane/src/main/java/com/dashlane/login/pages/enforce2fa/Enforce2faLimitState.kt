package com.dashlane.login.pages.enforce2fa

import com.dashlane.mvvm.State
import com.dashlane.udf.Event

sealed class Enforce2faLimitState : State {
    sealed class View : Enforce2faLimitState(), State.View {
        data object Limit : View()

        data class UpdateRedirectionFlag(val redirectionDone: Boolean) : View()

        data object Logout : View()
    }

    sealed class SideEffect : Enforce2faLimitState(), State.SideEffect {
        data object GoToLogin : SideEffect()
    }
}

sealed class Enforce2faLimitEvent : Event {
    data object ViewResumed : Enforce2faLimitEvent()

    data object ViewPaused : Enforce2faLimitEvent()

    data object RedirectionFlagUpdated : Enforce2faLimitEvent()

    data object LogoutClick : Enforce2faLimitEvent()

    data object GoToLogin : Enforce2faLimitEvent()
}