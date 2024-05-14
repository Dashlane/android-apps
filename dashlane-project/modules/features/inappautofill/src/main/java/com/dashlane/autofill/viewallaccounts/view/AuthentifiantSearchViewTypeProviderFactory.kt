package com.dashlane.autofill.viewallaccounts.view

import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapter.ItemListContextProvider
import com.dashlane.vault.summary.SummaryObject

interface AuthentifiantSearchViewTypeProviderFactory {
    fun create(
        matchedAuthentifiant: MatchedSearchResult,
        itemListContext: ItemListContext,
        query: String? = null
    ): AuthentifiantWrapperItem

    interface AuthentifiantWrapperItem : DashlaneRecyclerAdapter.ViewTypeProvider, ItemListContextProvider {
        fun getAuthentifiant(): SummaryObject.Authentifiant
    }
}
