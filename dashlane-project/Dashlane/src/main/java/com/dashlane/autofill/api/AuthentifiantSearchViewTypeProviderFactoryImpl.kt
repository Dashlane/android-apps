package com.dashlane.autofill.api

import com.dashlane.autofill.viewallaccounts.view.AuthentifiantSearchViewTypeProviderFactory
import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapters.text.factory.SearchListTextResolver
import com.dashlane.ui.screens.fragments.search.ui.SearchItemWrapper
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class AuthentifiantSearchViewTypeProviderFactoryImpl @Inject constructor(
    private val searchListTextResolver: SearchListTextResolver,
    private val itemWrapperProvider: ItemWrapperProvider,
) : AuthentifiantSearchViewTypeProviderFactory {

    override fun create(
        matchedAuthentifiant: MatchedSearchResult,
        itemListContext: ItemListContext,
        query: String?
    ): AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem =
        Wrapper(
            matchedAuthentifiant = matchedAuthentifiant,
            itemListContext = itemListContext,
            query = query,
            searchListTextResolver = searchListTextResolver,
            itemWrapperProvider = itemWrapperProvider
        )

    
    private class Wrapper(
        matchedAuthentifiant: MatchedSearchResult,
        itemListContext: ItemListContext,
        query: String?,
        searchListTextResolver: SearchListTextResolver,
        itemWrapperProvider: ItemWrapperProvider
    ) :
        SearchItemWrapper<SummaryObject.Authentifiant>(
            matchResult = matchedAuthentifiant,
            matchedText = query,
            searchListTextResolver = searchListTextResolver,
            itemWrapper = itemWrapperProvider.getAuthentifiantItemWrapper(
                matchedAuthentifiant.item as SummaryObject.Authentifiant,
                itemListContext
            )
        ),
        AuthentifiantSearchViewTypeProviderFactory.AuthentifiantWrapperItem {
        override fun getListItemActions(): List<ListItemAction> = emptyList()
        override fun getAuthentifiant(): SummaryObject.Authentifiant {
            return originalItemWrapper.summaryObject
        }
    }
}
