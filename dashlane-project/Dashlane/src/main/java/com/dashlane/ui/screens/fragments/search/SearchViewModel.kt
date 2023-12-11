package com.dashlane.ui.screens.fragments.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.loaders.datalists.SearchLoader
import com.dashlane.search.Match
import com.dashlane.search.MatchPosition
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.fields.LegacySearchField
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.ui.adapter.ItemListContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchLoader: SearchLoader,
    mainDataAccessor: MainDataAccessor
) : ViewModel() {

    private val frequentSearch = mainDataAccessor.getFrequentSearch()
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
                        SearchResult(
                            request,
                            frequentSearch.getLastSearchedItems(max = 100).map {
                                MatchedSearchResult(it, Match(MatchPosition.ANYWHERE, LegacySearchField.ANY_FIELD))
                            }
                        )
                    }
                }
            }
        }
    }

    fun searchFromQuery(request: SearchRequest) {
        searchFlow.tryEmit(request)
    }

    fun repeatLastSearch() {
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
