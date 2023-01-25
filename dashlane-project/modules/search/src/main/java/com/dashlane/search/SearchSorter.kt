package com.dashlane.search

interface SearchSorter {
    fun matchAndSort(all: List<Any>, query: String): List<MatchedSearchResult>

    fun match(item: Any, query: Query): Boolean
}