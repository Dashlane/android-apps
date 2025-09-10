package com.dashlane.search

import com.dashlane.ui.adapter.ItemListContext

sealed class SearchRequest {
    data class FromQuery(val query: String) : SearchRequest()

    data object FromRecent : SearchRequest()

    fun toSection(): ItemListContext.Section = when (this) {
        is FromQuery -> ItemListContext.Section.SEARCH_RESULT
        is FromRecent -> ItemListContext.Section.SEARCH_RECENT
    }
}