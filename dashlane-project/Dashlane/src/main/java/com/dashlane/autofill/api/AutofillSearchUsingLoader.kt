package com.dashlane.autofill.api

import android.content.Context
import com.dashlane.autofill.api.viewallaccounts.model.AutofillSearch
import com.dashlane.loaders.datalists.SearchLoader
import com.dashlane.search.Match
import com.dashlane.search.MatchPosition
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.SearchSorter
import com.dashlane.search.fields.LegacySearchField
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.ui.screens.fragments.search.util.SearchSorterProvider
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityUtil
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class AutofillSearchUsingLoader @Inject constructor(
    @ApplicationContext
    val applicationContext: Context,
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    dataIdentifierListTextResolver: DataIdentifierListTextResolver,
    identityUtil: IdentityUtil
) : AutofillSearch {
    private val searchLoader: SearchLoader
    private val searchSorter: SearchSorter = SearchSorterProvider
        .getSearchSorter(dataIdentifierListTextResolver, identityUtil)

    init {
        searchLoader = SearchLoader(searchSorter, applicationCoroutineScope)
    }

    override fun loadAuthentifiants(): List<SummaryObject.Authentifiant> =
        searchLoader.loadAuthentifiants()

    override fun loadAuthentifiant(authentifiantId: String): VaultItem<SyncObject.Authentifiant>? {
        return searchLoader.loadAuthentifiantById(authentifiantId)
    }

    override fun matchAuthentifiantsFromQuery(
        query: String?,
        items: List<SummaryObject.Authentifiant>
    ): List<MatchedSearchResult> = if (query == null) {
        items.map {
            MatchedSearchResult(
                it,
                Match(MatchPosition.START, LegacySearchField.ANY_FIELD)
            )
        }
    } else {
        searchSorter.matchAndSort(items, query)
    }
}
