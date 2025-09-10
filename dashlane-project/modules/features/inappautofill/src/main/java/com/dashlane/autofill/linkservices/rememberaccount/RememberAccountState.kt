package com.dashlane.autofill.linkservices.rememberaccount

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.mvvm.State

sealed class RememberAccountState : State {
    sealed class View : RememberAccountState(), State.View {
        object Initial : View()

        data class Loaded(
            val formSource: AutoFillFormSource,
            val title: String,
            val description: String,
            val itemIcon: AutofillFormSourceIcon,
            val itemName: String,
            val itemDescription: String?,
            val linkServiceIcon: AutofillFormSourceIcon,
            val linkServiceName: String,
            val linkServiceBadge: String,
            val primaryCtaText: String,
        ) : View()
    }

    sealed class SideEffect : RememberAccountState(), State.SideEffect {
        data class FinishWithResult(val shouldAutofill: Boolean) : SideEffect()
    }
}