package com.dashlane.ui.screens.fragments.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.loaders.datalists.FrequentSearchLoader
import com.dashlane.loaders.datalists.RecentSearchLoader
import com.dashlane.loaders.datalists.SearchLoader
import com.dashlane.search.Match
import com.dashlane.search.MatchPosition
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.fields.LegacySearchField
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.ui.screens.fragments.search.util.SearchSorterProvider
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.util.IdentityUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(val dataAccessor: MainDataAccessor, val userFeaturesChecker: UserFeaturesChecker) : ViewModel() {

    private val frequentSearchLoader = FrequentSearchLoader(viewModelScope)
    private val recentSearch = RecentSearchLoader(viewModelScope)

    private val searchLoader: SearchLoader

    init {
        val identityUtil = IdentityUtil(dataAccessor)
        val textResolver = DataIdentifierListTextResolver(identityUtil)
        searchLoader = SearchLoader(
            SearchSorterProvider.getSearchSorter(textResolver, identityUtil, userFeaturesChecker),
            viewModelScope
        )
    }

    private val _latestSearchResult = MutableLiveData<SearchResult?>(null)

    private val searchFlow = MutableSharedFlow<SearchRequest>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    internal val latestSearchResult: LiveData<SearchResult?>
        get() = _latestSearchResult

    val latestQuery: String?
        get() = (latestSearchResult.value?.searchRequest as? SearchRequest.FromQuery)?.query

    val resultCount: Int
        get() = latestSearchResult.value?.result?.size ?: 0

    init {
        viewModelScope.launch {
            searchFlow.collect { request ->
                _latestSearchResult.value = when (request) {
                    is SearchRequest.FromQuery -> SearchResult(request, searchLoader.filterByQuery(request.query))
                    is SearchRequest.DefaultRequest.FromRecent -> {
                        SearchResult(request, recentSearch.get()?.map {
                            MatchedSearchResult(it, Match(MatchPosition.ANYWHERE, LegacySearchField.ANY_FIELD))
                        } ?: listOf())
                    }
                }
            }
        }
    }

    fun reloadData() {
        searchLoader.refreshData()
        recentSearch.reloadData()
        frequentSearchLoader.reloadData()
    }

    fun searchFromQuery(request: SearchRequest) {
        searchFlow.tryEmit(request)
    }

    fun repeatLastSearch() {
        reloadData()
        latestSearchResult.value?.searchRequest?.let {
            searchFlow.tryEmit(it)
        }
    }
}

sealed class SearchRequest {

    data class FromQuery(val query: String) : SearchRequest()
    sealed class DefaultRequest : SearchRequest() {
        object FromRecent : DefaultRequest()
    }

    fun toSection(): ItemListContext.Section = when (this) {
        is FromQuery -> ItemListContext.Section.SEARCH_RESULT
        is DefaultRequest.FromRecent -> ItemListContext.Section.SEARCH_RECENT
    }
}
