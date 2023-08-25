package com.dashlane.autofill.api.rememberaccount.view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutofillLinkServiceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val autoFillDatabaseAccess: AutofillAnalyzerDef.DatabaseAccess
) : ViewModel() {

    private val args = AutofillLinkServiceFragmentArgs.fromSavedStateHandle(savedStateHandle)
    val state: StateFlow<AutofillLinkServiceState>
        get() = _state
    private val _state: MutableStateFlow<AutofillLinkServiceState> =
        MutableStateFlow(AutofillLinkServiceState.Initial)
    private var summary: SummaryObject.Authentifiant? = null

    init {
        viewModelScope.launch {
            autoFillDatabaseAccess.loadSummary<SummaryObject.Authentifiant>(args.itemId)?.let {
                summary = it
                _state.emit(AutofillLinkServiceState.OnDataLoaded(it, args.formSource))
            }
        }
    }

    fun getFormSource() = args.formSource

    fun getItemSummary() = summary
}