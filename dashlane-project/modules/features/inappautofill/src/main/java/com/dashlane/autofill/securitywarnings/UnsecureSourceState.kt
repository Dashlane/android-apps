package com.dashlane.autofill.securitywarnings

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.mvvm.State

sealed class UnsecureSourceState : State {
    data class View(
        val doNotShowAgainChecked: Boolean,
    ) : State.View

    sealed class SideEffect : UnsecureSourceState(), State.SideEffect {
        data class OnFillCancelled(
            val formSource: AutoFillFormSource,
        ) : SideEffect()

        data class OnFillAllowed(
            val formSource: AutoFillFormSource,
            val doNotShowAgainChecked: Boolean,
        ) : SideEffect()

        data class OnFillDenied(
            val formSource: AutoFillFormSource,
        ) : SideEffect()
    }
}