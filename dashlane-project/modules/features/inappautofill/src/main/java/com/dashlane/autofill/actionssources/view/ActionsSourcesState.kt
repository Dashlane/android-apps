package com.dashlane.autofill.actionssources.view

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.pause.model.AutofillFormSourceIcon
import com.dashlane.mvvm.State

sealed class ActionsSourcesState : State {
    sealed class View : ActionsSourcesState(), State.View {
        abstract val data: ActionsSourcesData

        data class Initial(override val data: ActionsSourcesData) : View()

        data class EmptyState(override val data: ActionsSourcesData) : View()

        data class Success(override val data: ActionsSourcesData) : View()

        data class Error(override val data: ActionsSourcesData) : View()
    }

    sealed class SideEffect : ActionsSourcesState(), State.SideEffect {
        data class OnServiceResumed(val message: String) : SideEffect()
    }
}

data class ActionsSourcesData(
    val temporarilyPausedItems: List<PausedItemData> = emptyList(),
    val permanentlyPausedItems: List<PausedItemData> = emptyList(),
)

data class PausedItemData(
    val form: AutoFillFormSource,
    val icon: AutofillFormSourceIcon,
    val title: String,
    val description: String?,
)