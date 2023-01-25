package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import com.dashlane.useractivity.log.usage.UsageLogCode80
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow



interface NewShareItemViewModelContract {
    val ulFrom: UsageLogCode80.From?
    val uiState: Flow<UIState>
    val selectionState: StateFlow<SelectionState>
    fun onQueryChange(query: String = ""): Boolean
    fun onClickNewShare()
    fun onItemSelected(uid: String, type: SyncObjectType)
    fun onItemUnSelected(uid: String, type: SyncObjectType)
    fun onBackPressed()
    fun onCreated()

    data class SelectionState(
        val accountsToShare: List<String>,
        val secureNotesToShare: List<String>
    ) {
        val totalCount: Int
            get() = accountsToShare.size + secureNotesToShare.size
    }

    sealed class UIState {
        object Loading : UIState()
        data class Data(val list: List<SummaryObject>, val query: String? = null) : UIState()
    }
}