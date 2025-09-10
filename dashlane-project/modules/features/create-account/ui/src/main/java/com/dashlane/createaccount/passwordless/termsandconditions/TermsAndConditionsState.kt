package com.dashlane.createaccount.passwordless.termsandconditions

import android.net.Uri
import com.dashlane.mvvm.State

sealed class TermsAndConditionsState : State {
    data class View(
        val isTosChecked: Boolean = false,
        val isOffersChecked: Boolean = false,
        val isContinueEnabled: Boolean = false,
    ) : TermsAndConditionsState(), State.View

    sealed class SideEffect : TermsAndConditionsState(), State.SideEffect {
        data object Success : SideEffect()

        data class GoToHelp(val uri: Uri) : SideEffect()
    }
}