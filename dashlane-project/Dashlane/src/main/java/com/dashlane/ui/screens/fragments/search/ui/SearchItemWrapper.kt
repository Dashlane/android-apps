package com.dashlane.ui.screens.fragments.search.ui

import android.content.Context
import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemDoubleWrapper
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.ui.adapters.text.factory.SearchListTextResolver
import com.dashlane.vault.summary.SummaryObject

open class SearchItemWrapper<D : SummaryObject>(
    private val matchResult: MatchedSearchResult,
    private val matchedText: String?,
    private val searchListTextResolver: SearchListTextResolver,
    itemWrapper: VaultItemWrapper<D>,
) : VaultItemDoubleWrapper<D>(itemWrapper) {

    override fun getTitle(context: Context): StatusText = if (matchedText == null) {
        searchListTextResolver.getLine1(context, originalItemWrapper.summaryObject)
    } else {
        searchListTextResolver.getHighlightedLine1(
            context,
            originalItemWrapper.summaryObject,
            matchedText
        )
    }

    override fun getDescription(context: Context): StatusText = if (matchedText == null) {
        searchListTextResolver.getLine2(context, originalItemWrapper.summaryObject)
    } else {
        searchListTextResolver.getHighlightedLine2(
            context,
            originalItemWrapper.summaryObject,
            matchedText,
            matchResult.match.field
        )
    }
}