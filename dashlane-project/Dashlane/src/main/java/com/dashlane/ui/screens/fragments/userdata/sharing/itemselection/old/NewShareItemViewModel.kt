package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection.old

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.loaders.datalists.NewShareItemDataProvider
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockWatcher
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.navigation.Navigator
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.isProtected
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewShareItemViewModel @Inject constructor(
    private val dataProvider: NewShareItemDataProvider,
    private val navigator: Navigator,
    private val lockWatcher: LockWatcher,
) : ViewModel() {
    private val _uiState: MutableViewStateFlow<NewShareItemUIState.View, NewShareItemUIState.SideEffect> =
        MutableViewStateFlow(NewShareItemUIState.View())
    val uiState: ViewStateFlow<NewShareItemUIState.View, NewShareItemUIState.SideEffect> = _uiState

    init {
        viewModelScope.launch {
            dataProvider.init()
            query()
        }
    }

    fun onQueryChange(query: String = ""): Boolean {
        query(query)
        return true
    }

    fun onClickNewShare() {
        val state = _uiState.value
        if (state.selectionForSharing.any { it.isProtected }) {
            lockWatcher.lockEventFlow
                .filterIsInstance<LockEvent.Unlock>()
                .map { it.reason }
                .filter { it is LockEvent.Unlock.Reason.WithCode && it.requestCode == UNLOCK_EVENT_CODE }
                .take(1)
                .onStart { _uiState.send(NewShareItemUIState.SideEffect.MPLock) }
                .onEach {
                    goToNewShare(_uiState.value.selectionForSharing)
                }
                .launchIn(viewModelScope)
        } else {
            goToNewShare(state.selectionForSharing)
        }
    }

    private fun goToNewShare(selection: List<SummaryObject>) {
        navigator.goToPeopleSelectionFromNewShare(
            selectedPasswords = selection
                .filterIsInstance<SummaryObject.Authentifiant>()
                .map { it.id }.toTypedArray(),
            selectedNotes = selection
                .filterIsInstance<SummaryObject.SecureNote>()
                .map { it.id }.toTypedArray(),
            
            selectedSecrets = emptyArray(),
        )
    }

    fun onItemSelected(summaryObject: SummaryObject) {
        _uiState.update { current ->
            current.copy(
                selectionForSharing = current.selectionForSharing + summaryObject,
                showFab = true,
            )
        }
    }

    fun onItemUnSelected(summaryObject: SummaryObject) {
        _uiState.update { current ->
            val newSelection = current.selectionForSharing.filter { it.id != summaryObject.id }
            current.copy(
                selectionForSharing = newSelection,
                showFab = newSelection.isNotEmpty(),
            )
        }
    }

    fun query(query: String? = null) {
        viewModelScope.launch {
            val d1 = async { dataProvider.loadAccounts(query) }
            val d2 = async { dataProvider.loadSecureNotes(query) }
            _uiState.update {
                it.copy(list = (d1.await() + d2.await()), query = query)
            }
        }
    }

    companion object {
        const val UNLOCK_EVENT_CODE = 84271
    }
}