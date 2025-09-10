package com.dashlane.search.helper

import android.content.Context
import com.dashlane.search.Match
import com.dashlane.search.MatchPosition
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.R
import com.dashlane.search.SearchListItem
import com.dashlane.search.SearchListItem.Item.Vault
import com.dashlane.search.SearchRequest
import com.dashlane.search.SearchSorter
import com.dashlane.search.SearchVaultListItemState
import com.dashlane.search.fields.LegacySearchField
import com.dashlane.search.textfactory.SearchListTextResolver
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.item.VaultListItemStateFactory
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SearchResultHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vaultListItemStateFactory: VaultListItemStateFactory,
    private val searchListTextResolver: SearchListTextResolver,
    private val frequentSearch: FrequentSearch,
    private val searchSorter: SearchSorter,
) {
    suspend fun buildRecentSearchResult(): List<SearchListItem> {
        val lastSearched = frequentSearch.getLastSearchedItems(max = 100)
        val matchList = lastSearched.map {
            MatchedSearchResult(
                it,
                Match(MatchPosition.ANYWHERE, LegacySearchField.ANY_FIELD),
            )
        }
        return buildSearchResult(
            items = matchList,
            request = SearchRequest.FromRecent,
        )
    }

    fun buildFromQuery(
        vaultItems: List<SummaryObject>,
        queryRequest: SearchRequest.FromQuery,
    ): List<SearchListItem> {
        val result = searchSorter.matchAndSort(vaultItems, queryRequest.query)
        return buildSearchResult(result, queryRequest)
    }

    private fun buildSearchResult(
        items: List<MatchedSearchResult>,
        request: SearchRequest,
    ): List<SearchListItem> {
        val list: MutableList<SearchListItem> = ArrayList()
        val resultCount = items.size

        addHeaderIfNecessary(resultList = list, request = request, matchedItems = items)

        
        val baseListContext =
            ItemListContext.Container.SEARCH.asListContext(section = request.toSection())

        items.forEachIndexed { i, matchResult ->
            getItemToAdd(
                matchResult = matchResult,
                container = baseListContext.copy(i, resultCount),
                query = (request as? SearchRequest.FromQuery)?.query,
            )?.let { itemToAdd ->
                list.add(itemToAdd)
            }
        }
        return list
    }

    private fun addHeaderIfNecessary(
        resultList: MutableList<SearchListItem>,
        request: SearchRequest,
        matchedItems: List<MatchedSearchResult>,
    ) {
        val header = when (request) {
            SearchRequest.FromRecent -> SearchListItem.Header(
                context.getString(R.string.search_screen_header_last_searched),
            ).takeUnless { matchedItems.isEmpty() }
            is SearchRequest.FromQuery -> null
        }
        header?.let { resultList.add(it) }
    }

    private fun getItemToAdd(
        matchResult: MatchedSearchResult,
        container: ItemListContext,
        query: String?,
    ): SearchListItem? {
        val item = matchResult.item
        if (item is SummaryObject) {
            val description = searchListTextResolver.getHighlightedLine2(
                item = item,
                targetText = query,
                searchField = matchResult.match.field,
            )
            return Vault(
                itemState = SearchVaultListItemState(
                    itemId = item.id,
                    title = searchListTextResolver.getHighlightedLine1(
                        item = item,
                        targetText = query,
                    ).text,
                    description = description.text,
                    highlightText = query,
                    hasError = description.isWarning,
                    thumbnail = vaultListItemStateFactory.buildThumbnailType(item),
                ),
                summaryObject = item,
                itemListContext = container,
            )
        }
        return null
    }
}