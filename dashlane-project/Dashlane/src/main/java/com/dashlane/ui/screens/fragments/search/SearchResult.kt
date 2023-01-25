package com.dashlane.ui.screens.fragments.search

import com.dashlane.search.MatchedSearchResult

data class SearchResult(val searchRequest: SearchRequest, val result: List<MatchedSearchResult>)