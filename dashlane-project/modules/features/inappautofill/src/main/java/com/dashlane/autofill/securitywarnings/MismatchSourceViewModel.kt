package com.dashlane.autofill.securitywarnings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.securitywarnings.view.BottomSheetMismatchSecurityWarningDialogFragment
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MismatchSourceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val applicationFormSource: ApplicationFormSource? =
        savedStateHandle[BottomSheetMismatchSecurityWarningDialogFragment.ARG_FORM_SOURCE]
    private val _stateFlow: MutableViewStateFlow<MismatchSourceState.View, MismatchSourceState.SideEffect>
    val stateFlow: ViewStateFlow<MismatchSourceState.View, MismatchSourceState.SideEffect>

    init {
        val formSourceLabel: String = savedStateHandle[BottomSheetMismatchSecurityWarningDialogFragment.ARG_FORM_SOURCE_LABEL]
            ?: throw IllegalStateException("No form source label provided to BottomSheetMismatchSecurityWarningDialogFragment")
        val credentialToFill: AuthentifiantItemToFill = savedStateHandle[BottomSheetMismatchSecurityWarningDialogFragment.ARG_ITEM_TO_FILL]
            ?: throw IllegalStateException("No authentifiant item to fill provided to BottomSheetMismatchSecurityWarningDialogFragment")
        this._stateFlow = MutableViewStateFlow(
            MismatchSourceState.View(
                credentialTitle = credentialToFill.title ?: "",
                credentialUrl = credentialToFill.url,
                credentialLogin = credentialToFill.login ?: "",
                applicationName = formSourceLabel,
                doNotShowAgainChecked = false,
            ),
        )
        this.stateFlow = _stateFlow
    }

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
                MismatchSourceState.SideEffect.OnFillCancelled(
                    applicationFormSource = applicationFormSource,
                ),
            )
        }
    }

    fun onFillAllowed(
        doNotShowAgainChecked: Boolean,
    ) {
        viewModelScope.launch {
            _stateFlow.send(
                MismatchSourceState.SideEffect.OnFillAllowed(
                    applicationFormSource = applicationFormSource,
                    doNotShowAgainChecked = doNotShowAgainChecked,
                ),
            )
        }
    }

    fun onFillDenied() {
        viewModelScope.launch {
            _stateFlow.send(
                MismatchSourceState.SideEffect.OnFillDenied(
                    applicationFormSource = applicationFormSource,
                ),
            )
        }
    }
}