package com.dashlane.autofill.frozenautofill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.api.R
import com.dashlane.autofill.frozenautofill.FrozenAutofillState.BottomSheetState
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.frozenaccount.tracking.FrozenStateLogger
import com.dashlane.ui.model.TextResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FrozenAutofillViewModel @Inject constructor(
    frozenStateManager: FrozenStateManager,
    private val frozenStateLogger: FrozenStateLogger,
) : ViewModel() {

    private val stateFlow = MutableStateFlow(
        FrozenAutofillState(
            description = TextResource.StringText(
                stringRes = R.string.autofill_frozen_bottom_sheet_description,
                arg = TextResource.Arg.StringArg(frozenStateManager.passwordLimitCount.toString())
            ),
            bottomSheetState = BottomSheetState.FOLDED
        )
    )
    val uiState = stateFlow.asStateFlow()

    fun onViewReady() {
        viewModelScope.launch {
            frozenStateLogger.logAutofillPaywallDisplayed()
            stateFlow.update { state ->
                state.copy(bottomSheetState = BottomSheetState.EXPANDED)
            }
        }
    }

    fun onNotNowClicked() {
        frozenStateLogger.logNotNowClicked()
        onFinishing()
    }

    fun onUnfreezeAccountClicked() {
        frozenStateLogger.logUnfreezeAccountClicked()
        onFinishing()
    }

    private fun onFinishing() {
        viewModelScope.launch {
            stateFlow.update { state ->
                state.copy(bottomSheetState = BottomSheetState.FOLDED)
            }
        }
    }
}