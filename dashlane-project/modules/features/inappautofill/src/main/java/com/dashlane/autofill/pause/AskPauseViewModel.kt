package com.dashlane.autofill.pause

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AskPauseViewModel @Inject constructor(
    private val repository: AskPauseRepository,
) : ViewModel() {
    private val _stateFlow: MutableViewStateFlow<AskPauseState.View, AskPauseState.SideEffect> =
        MutableViewStateFlow(AskPauseState.View.Initial)
    val stateFlow: ViewStateFlow<AskPauseState.View, AskPauseState.SideEffect> = _stateFlow

    private var pausedFormSource: AutoFillFormSource? = null
    private var selectedPauseDuration: PauseDurations? = null

    fun load(formSource: AutoFillFormSource) {
        this.pausedFormSource = formSource
        viewModelScope.launch {
            try {
                val title = repository.getTitle()
                val icon = repository.getIcon(formSource)
                val serviceName = repository.getServiceName(formSource)
                _stateFlow.update { _ ->
                    AskPauseState.View.Loaded(
                        viewData = AskPauseData(
                            title = title,
                            icon = icon,
                            serviceName = serviceName,
                            selectedPauseDuration = selectedPauseDuration,
                        ),
                    )
                }
            } catch (e: Exception) {
                _stateFlow.update { _ -> AskPauseState.View.Hidden }
                _stateFlow.send(AskPauseState.SideEffect.OnError(repository.getPauseMessageForError()))
            }
        }
    }

    fun onPauseOptionClick(pauseDurations: PauseDurations) {
        selectedPauseDuration = if (selectedPauseDuration == pauseDurations) {
            null
        } else {
            pauseDurations
        }
        viewModelScope.launch {
            _stateFlow.update { stateView ->
                when (stateView) {
                    is AskPauseState.View.Loaded -> {
                        stateView.copy(
                            viewData = stateView.viewData.copy(
                                selectedPauseDuration = selectedPauseDuration,
                            ),
                        )
                    }
                    else -> {
                        
                        stateView
                    }
                }
            }
        }
    }

    fun onDoneClick() {
        val formSource = this.pausedFormSource
        val pauseDuration = this.selectedPauseDuration
        viewModelScope.launch {
            _stateFlow.update { _ -> AskPauseState.View.Hidden }
            try {
                requireNotNull(formSource)
                requireNotNull(pauseDuration)
                repository.pauseItem(formSource, pauseDuration)
                val message = repository.getPauseMessageForDuration(formSource, pauseDuration)
                _stateFlow.send(AskPauseState.SideEffect.OnSuccess(message))
            } catch (e: Exception) {
                _stateFlow.send(AskPauseState.SideEffect.OnError(repository.getPauseMessageForError()))
            }
        }
    }

    fun onDismiss() {
        viewModelScope.launch {
            _stateFlow.update { _ -> AskPauseState.View.Hidden }
            _stateFlow.send(AskPauseState.SideEffect.OnCancel)
        }
    }
}