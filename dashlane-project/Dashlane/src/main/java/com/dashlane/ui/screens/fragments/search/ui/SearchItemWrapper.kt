package com.dashlane.ui.screens.fragments.search.ui

import android.content.Context
import com.dashlane.search.MatchedSearchResult
import com.dashlane.search.textfactory.SearchListTextResolver
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemDoubleWrapper
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.StatusText

open class SearchItemWrapper<D : SummaryObject>(
    private val matchResult: MatchedSearchResult,
    private val matchedText: String?,
    private val searchListTextResolver: SearchListTextResolver,
    itemWrapper: VaultItemWrapper<D>,
) : VaultItemDoubleWrapper<D>(itemWrapper) {

    override fun getTitle(context: Context): StatusText = if (matchedText == null) {
        searchListTextResolver.getLine1(originalItemWrapper.summaryObject)
    } else {
        searchListTextResolver.getHighlightedLine1(
            originalItemWrapper.summaryObject,
            matchedText
        )
    }

    override fun getDescription(context: Context): StatusText = if (matchedText == null) {
        searchListTextResolver.getLine2(originalItemWrapper.summaryObject)
    } else {
        searchListTextResolver.getHighlightedLine2(
            originalItemWrapper.summaryObject,
            matchedText,
            matchResult.match.field
        )
    }
}