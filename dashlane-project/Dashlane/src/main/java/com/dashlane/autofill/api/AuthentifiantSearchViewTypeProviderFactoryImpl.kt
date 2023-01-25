package com.dashlane.autofill.api

import com.dashlane.autofill.api.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.wrapper.DefaultVaultItemWrapper
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.screens.fragments.search.ui.SearchItemWrapper
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject



class AuthentifiantSearchViewTypeProviderFactoryImpl @Inject constructor() :
    AuthentifiantSearchViewTypeProviderFactory {

    override fun create(
        matchedAuthentifiant: MatchedSearchResult,
        itemListContext: ItemListContext,
        query: String?
    ): AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem =
        Wrapper(
            matchedAuthentifiant = matchedAuthentifiant,
            itemListContext = itemListContext,
            query = query
        )

    
    private class Wrapper(
        matchedAuthentifiant: MatchedSearchResult,
        itemListContext: ItemListContext,
        query: String?
    ) : SearchItemWrapper<SummaryObject.Authentifiant>(
        matchResult = matchedAuthentifiant,
        matchedText = query,
        itemWrapper = DefaultVaultItemWrapper(
            matchedAuthentifiant.item as SummaryObject.Authentifiant,
            itemListContext
        )
    ), AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem {
        override fun getListItemActions(): List<ListItemAction> = emptyList()
        override fun getAuthentifiant(): SummaryObject.Authentifiant {
            return originalItemWrapper.itemObject
        }
    }
}
