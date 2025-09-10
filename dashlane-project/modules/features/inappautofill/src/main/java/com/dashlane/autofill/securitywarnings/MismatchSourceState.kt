package com.dashlane.autofill.securitywarnings

import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.mvvm.State

sealed class MismatchSourceState : State {
    data class View(
        val credentialTitle: String,
        val credentialUrl: String?,
        val credentialLogin: String,
        val applicationName: String,
        val doNotShowAgainChecked: Boolean,
    ) : State.View

    sealed class SideEffect : MismatchSourceState(), State.SideEffect {
        data class OnFillCancelled(
            val applicationFormSource: ApplicationFormSource?,
        ) : SideEffect()

        data class OnFillAllowed(
            val applicationFormSource: ApplicationFormSource?,
            val doNotShowAgainChecked: Boolean,
        ) : SideEffect()

        data class OnFillDenied(
            val applicationFormSource: ApplicationFormSource?,
        ) : SideEffect()
    }
}