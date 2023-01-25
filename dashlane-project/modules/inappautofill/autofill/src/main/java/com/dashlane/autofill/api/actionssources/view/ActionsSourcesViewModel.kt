package com.dashlane.autofill.api.actionssources.view

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.api.actionssources.model.ActionsSourcesDataProvider
import com.dashlane.autofill.api.actionssources.model.ActionsSourcesError
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.logD
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ActionsSourcesViewModel @Inject constructor(
    private val dataProvider: ActionsSourcesDataProvider,
    private val autofillFormSourceViewTypeProviderFactory: AutofillFormSourceViewTypeProviderFactory,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val selectedItemMutableFLow = MutableSharedFlow<AutoFillFormSource>()
    private val actionsSourcesStateMutableFlow = MutableStateFlow<ActionsSourcesState>(ActionsSourcesState.Initial(ActionsSourcesData()))

    val actionsSourcesStateFlow = actionsSourcesStateMutableFlow.asStateFlow()
    val selectedItemFlow: Flow<AutoFillFormSource> = selectedItemMutableFLow.asSharedFlow()

    private var hasNavigated: Boolean = false

    fun viewResumed() {
        if (actionsSourcesStateMutableFlow.value is ActionsSourcesState.Initial || hasNavigated) loadFormSources()
        hasNavigated = false
    }

    fun onRefresh() {
        loadFormSources()
    }

    fun onFormSourcesItemClick(position: Int) {
        viewModelScope.launch(defaultDispatcher) {
            try {
                val selectedFormSource = dataProvider.selectFormSourceItem(position)
                hasNavigated = true
                selectedItemMutableFLow.emit(selectedFormSource)
            } catch (e: IllegalStateException) {
                logD("ActionsSourcesViewModel", e.message ?: "", e)
                actionsSourcesStateMutableFlow.update { previousState ->
                    ActionsSourcesState.Error(previousState.data, ActionsSourcesError.AllSelectItem)
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun loadFormSources() {
        viewModelScope.launch(defaultDispatcher) {
            actionsSourcesStateMutableFlow.update { previousState -> ActionsSourcesState.Loading(previousState.data.copy(isLoading = true)) }

            val formSourceResult = dataProvider.loadFormSources()

            formSourceResult.getOrNull()
                ?.let { allFormSources -> allFormSources.map { autofillFormSourceViewTypeProviderFactory.create(it) } }
                ?.let { autofillFormSourceWrappers ->
                    actionsSourcesStateMutableFlow.update { previousState ->
                        ActionsSourcesState.Success(previousState.data.copy(isLoading = false, itemList = autofillFormSourceWrappers))
                    }
                }

            formSourceResult.exceptionOrNull()
                ?.let { error ->
                    actionsSourcesStateMutableFlow.update { previousState ->
                        ActionsSourcesState.Error(previousState.data.copy(isLoading = false), error as ActionsSourcesError)
                    }
                }
        }
    }
}
