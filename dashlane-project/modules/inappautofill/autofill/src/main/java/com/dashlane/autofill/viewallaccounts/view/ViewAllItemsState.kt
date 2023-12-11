package com.dashlane.autofill.viewallaccounts.view

import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

sealed class ViewAllItemsState {
    object Initial : ViewAllItemsState()
    object Loading : ViewAllItemsState()
    data class Error(val message: String) : ViewAllItemsState()
    data class Loaded(val data: List<MatchedSearchResult>, val query: String) : ViewAllItemsState()
    data class Selected(
        val selectedCredential: VaultItem<SyncObject.Authentifiant>,
        val itemListContext: ItemListContext
    ) : ViewAllItemsState()
}