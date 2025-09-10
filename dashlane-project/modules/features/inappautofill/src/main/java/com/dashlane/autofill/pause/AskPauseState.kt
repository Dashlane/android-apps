package com.dashlane.autofill.pause

import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.mvvm.State

sealed class AskPauseState : State {
    sealed class View : AskPauseState(), State.View {
        data object Initial : View()

        data class Loaded(val viewData: AskPauseData) : View()

        data object Hidden : View()
    }

    sealed class SideEffect : AskPauseState(), State.SideEffect {
        data class OnError(val message: String) : SideEffect()

        data class OnSuccess(val message: String) : SideEffect()

        data object OnCancel : SideEffect()
    }
}

data class AskPauseData(
    val title: String,
    val icon: AutofillFormSourceIcon,
    val serviceName: String,
    val selectedPauseDuration: PauseDurations?,
)