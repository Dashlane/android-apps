package com.dashlane.ui.screens.fragments.search.ui

import android.content.Context
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.search.MatchedSearchResult
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemDoubleWrapper
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextFactory.StatusText
import com.dashlane.ui.adapters.text.factory.SearchListTextResolver
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityUtil

open class SearchItemWrapper<D : SummaryObject>(
    private val matchResult: MatchedSearchResult,
    private val matchedText: String?,
    itemWrapper: VaultItemWrapper<D>,
) : VaultItemDoubleWrapper<D>(itemWrapper) {

    private val searchListTextResolver = SearchListTextResolver(IdentityUtil(SingletonProvider.getMainDataAccessor()))

    override fun getTitle(context: Context): StatusText = if (matchedText == null) {
        searchListTextResolver.getLine1(context, originalItemWrapper.itemObject)
    } else {
        searchListTextResolver.getHighlightedLine1(
            context,
            originalItemWrapper.itemObject,
            matchedText
        )
    }

    override fun getDescription(context: Context): StatusText = if (matchedText == null) {
        searchListTextResolver.getLine2(context, originalItemWrapper.itemObject)
    } else {
        searchListTextResolver.getHighlightedLine2(
            context,
            originalItemWrapper.itemObject,
            matchedText,
            matchResult.match.field
        )
    }
}