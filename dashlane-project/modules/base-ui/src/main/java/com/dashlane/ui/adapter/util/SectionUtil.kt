package com.dashlane.ui.adapter.util

import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.ui.adapter.ItemListContext

fun ItemListContext.Section.toHighlight() = when (this) {
    ItemListContext.Section.MOST_RECENT -> Highlight.MOST_RECENT
    ItemListContext.Section.SUGGESTED -> Highlight.SUGGESTED
    ItemListContext.Section.SEARCH_RECENT -> Highlight.SEARCH_RECENT
    ItemListContext.Section.SEARCH_RESULT -> Highlight.SEARCH_RESULT
    ItemListContext.Section.ALPHABETICAL, ItemListContext.Section.CATEGORY, ItemListContext.Section.NONE -> Highlight.NONE
}
