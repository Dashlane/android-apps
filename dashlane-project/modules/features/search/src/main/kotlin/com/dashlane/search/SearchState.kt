package com.dashlane.search

import com.dashlane.mvvm.State
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType

sealed class SearchState : State {
    sealed class ViewState : State.View {
        abstract val isLoading: Boolean
        abstract val searchRequest: SearchRequest
        abstract val items: List<SearchListItem>

        data class SearchResult(
            override val isLoading: Boolean,
            override val searchRequest: SearchRequest,
            override val items: List<SearchListItem>,
        ) : ViewState(), State.View

        data class EmptyState(
            override val isLoading: Boolean,
            override val searchRequest: SearchRequest,
        ) : ViewState(), State.View {
            override val items: List<SearchListItem>
                get() = emptyList()
        }
    }

    sealed class SideEffect : State.SideEffect {
        data class OpenItemDetails(val itemId: String, val itemType: SyncObjectType) : SideEffect()

        data class OpenQuickActions(val itemId: String, val itemListContext: ItemListContext) :
            SideEffect()
    }
}

sealed class SearchListItem {
    class Header(val text: String) : SearchListItem()

    sealed class Item : SearchListItem() {
        abstract val itemListContext: ItemListContext

        data class Vault(
            val itemState: SearchVaultListItemState,
            val summaryObject: SummaryObject,
            override val itemListContext: ItemListContext,
        ) : Item()
    }
}