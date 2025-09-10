package com.dashlane.autofill.securitywarnings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.securitywarnings.view.IncorrectSecurityWarningDialogFragment
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnsecureSourceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val formSource: AutoFillFormSource =
        savedStateHandle[IncorrectSecurityWarningDialogFragment.ARG_FORM_SOURCE]
            ?: throw IllegalStateException("No form source provided to IncorrectSecurityWarningDialogFragment")

    private val _stateFlow: MutableViewStateFlow<UnsecureSourceState.View, UnsecureSourceState.SideEffect> =
        MutableViewStateFlow(
            UnsecureSourceState.View(
                doNotShowAgainChecked = false,
            ),
        )
    val stateFlow: ViewStateFlow<UnsecureSourceState.View, UnsecureSourceState.SideEffect> = _stateFlow

    fun onDoNotShowAgainToggleChange() {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                previousState.copy(
                    doNotShowAgainChecked = !previousState.doNotShowAgainChecked,
                )
            }
        }
    }

    fun onFillCancelled() {
        viewModelScope.launch {
            _stateFlow.send(
                UnsecureSourceState.SideEffect.OnFillCancelled(
                    formSource = formSource,
                ),
            )
        }
    }

    fun onFillAllowed(
        doNotShowAgainChecked: Boolean,
    ) {
        viewModelScope.launch {
            _stateFlow.send(
                UnsecureSourceState.SideEffect.OnFillAllowed(
                    formSource = formSource,
                    doNotShowAgainChecked = doNotShowAgainChecked,
                ),
            )
        }
    }

    fun onFillDenied() {
        viewModelScope.launch {
            _stateFlow.send(
                UnsecureSourceState.SideEffect.OnFillDenied(
                    formSource = formSource,
                ),
            )
        }
    }
}