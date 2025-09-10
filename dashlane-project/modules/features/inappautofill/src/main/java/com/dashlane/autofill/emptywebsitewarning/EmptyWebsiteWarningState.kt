package com.dashlane.autofill.emptywebsitewarning

import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.mvvm.State

sealed class EmptyWebsiteWarningState : State {
    sealed class View : EmptyWebsiteWarningState(), State.View {
        data object Initial : View()

        data class Loaded(val viewData: EmptyWebsiteWarningData) : View()

        data object Hidden : View()
    }

    sealed class SideEffect : EmptyWebsiteWarningState(), State.SideEffect {
        data class OnSuccess(val message: String, val itemToFill: AuthentifiantItemToFill) : SideEffect()

        data object OnError : SideEffect()

        data object OnCancel : SideEffect()
    }
}

data class EmptyWebsiteWarningData(
    val credentialTitle: String,
    val credentialLogin: String,
    val websiteToAdd: String,
)