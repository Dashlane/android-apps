package com.dashlane.autofill.api

import com.dashlane.autofill.viewallaccounts.AutofillSearch
import com.dashlane.loaders.datalists.SearchLoader
import com.dashlane.search.Match
import com.dashlane.search.MatchPosition
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.SearchSorter
import com.dashlane.search.fields.LegacySearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.ui.screens.fragments.search.util.SearchSorterProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityNameHolderService
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class AutofillSearchUsingLoader @Inject constructor(
    searchSorterProvider: SearchSorterProvider,
    dataIdentifierListTextResolver: DataIdentifierListTextResolver,
    identityNameHolderService: IdentityNameHolderService,
    private val searchLoader: SearchLoader
) : AutofillSearch {
    private val searchSorter: SearchSorter = searchSorterProvider
        .getSearchSorter(dataIdentifierListTextResolver, identityNameHolderService)

    override suspend fun loadAuthentifiants(): List<SummaryObject.Authentifiant> =
        searchLoader.loadCredentials()

    override suspend fun loadAuthentifiant(authentifiantId: String): VaultItem<SyncObject.Authentifiant>? {
        return searchLoader.loadCredentialById(authentifiantId)
    }

    override fun matchAuthentifiantsFromQuery(
        query: String?,
        items: List<SummaryObject.Authentifiant>
    ): List<MatchedSearchResult> = if (query == null) {
        items.map {
            MatchedSearchResult(it, Match(MatchPosition.START, LegacySearchField.ANY_FIELD))
        }
    } else {
        searchSorter.matchAndSort(items, query)
    }
}
