package com.dashlane.autofill.api.actionssources.view

import com.dashlane.autofill.api.actionssources.model.ActionsSourcesError

sealed class ActionsSourcesState {
    abstract val data: ActionsSourcesData

    data class Initial(override val data: ActionsSourcesData) : ActionsSourcesState()
    data class Loading(override val data: ActionsSourcesData) : ActionsSourcesState()
    data class Success(override val data: ActionsSourcesData) : ActionsSourcesState()
    data class Error(override val data: ActionsSourcesData, val error: ActionsSourcesError) : ActionsSourcesState()
}

data class ActionsSourcesData(
    val isLoading: Boolean = false,
    val itemList: List<AutofillFormSourceViewTypeProviderFactory.AutofillFormSourceWrapper> = emptyList()
)
