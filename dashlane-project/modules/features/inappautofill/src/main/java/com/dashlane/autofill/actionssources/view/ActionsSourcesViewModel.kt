package com.dashlane.autofill.actionssources.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.actionssources.model.ActionsSourcesRepository
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.pause.services.RemovePauseContract
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActionsSourcesViewModel @Inject constructor(
    private val removePauseContract: RemovePauseContract,
    private val actionsSourcesRepository: ActionsSourcesRepository,
    private val logRepository: LogRepository,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _stateFlow: MutableViewStateFlow<ActionsSourcesState.View, ActionsSourcesState.SideEffect> =
        MutableViewStateFlow(
            ActionsSourcesState.View.Initial(
                data = ActionsSourcesData(
                    temporarilyPausedItems = emptyList(),
                    permanentlyPausedItems = emptyList(),
                ),
            ),
        )
    val stateFlow: ViewStateFlow<ActionsSourcesState.View, ActionsSourcesState.SideEffect> = _stateFlow

    init {
        loadFormSources()
        logCurrentPageView()
    }

    fun onRefresh() {
        loadFormSources()
    }

    fun onResumeClick(form: AutoFillFormSource) {
        viewModelScope.launch {
            removePauseContract.removePause(form)
            val message = actionsSourcesRepository.getResumedMessageFor(form)
            _stateFlow.send(ActionsSourcesState.SideEffect.OnServiceResumed(message = message))
            loadFormSources()
        }
    }

    private fun loadFormSources() {
        viewModelScope.launch(defaultDispatcher) {
            val formSourcesResult = actionsSourcesRepository.loadFormSources().getOrNull()
            _stateFlow.update { previousState ->
                when (formSourcesResult != null) {
                    true -> {
                        val (temporarilyPausedItems, permanentlyPausedItems) = formSourcesResult
                        if (temporarilyPausedItems.isEmpty() && permanentlyPausedItems.isEmpty()) {
                            ActionsSourcesState.View.EmptyState(
                                data = ActionsSourcesData(
                                    temporarilyPausedItems = emptyList(),
                                    permanentlyPausedItems = emptyList(),
                                ),
                            )
                        } else {
                            ActionsSourcesState.View.Success(
                                data = ActionsSourcesData(
                                    temporarilyPausedItems = temporarilyPausedItems,
                                    permanentlyPausedItems = permanentlyPausedItems,
                                ),
                            )
                        }
                    }
                    else -> {
                        ActionsSourcesState.View.Error(
                            data = ActionsSourcesData(
                                temporarilyPausedItems = previousState.data.temporarilyPausedItems,
                                permanentlyPausedItems = previousState.data.permanentlyPausedItems,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun logCurrentPageView() {
        logRepository.queuePageView(
            component = BrowseComponent.MAIN_APP,
            page = AnyPage.SETTINGS_AUTOFILL,
        )
    }
}