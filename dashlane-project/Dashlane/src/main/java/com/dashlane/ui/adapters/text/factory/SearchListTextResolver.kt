package com.dashlane.ui.adapters.text.factory

import android.content.Context
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.search.SearchField
import com.dashlane.util.ignoreEscapedCharacter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.IdentityUtil

class SearchListTextResolver(identityUtil: IdentityUtil = IdentityUtil(SingletonProvider.getMainDataAccessor())) :
    DataIdentifierListTextResolver(identityUtil) {

    fun getHighlightedLine1(
        context: Context,
        item: SummaryObject,
        targetText: String
    ): DataIdentifierListTextFactory.StatusText {
        val primaryLine = getTextFactory(context, item).getLine1()
        return DataIdentifierListTextFactory.StatusText(
            primaryLine.text,
            primaryLine.isWarning,
            targetText
        )
    }

    fun getHighlightedLine2(
        context: Context,
        item: SummaryObject,
        targetText: String,
        searchField: SearchField<*>
    ): DataIdentifierListTextFactory.StatusText {
        val secondaryLine = getTextFactory(context, item).getLine2FromField(searchField)
            ?: return super.getLine2(context, item)

        return DataIdentifierListTextFactory.StatusText(secondaryLine.text.ignoreEscapedCharacter().focusOn(targetText),
            secondaryLine.isWarning,
            targetText
        )
    }

    private fun String.focusOn(targetText: String): String {

        val startIndex = this.indexOf(targetText, ignoreCase = true)
        if (startIndex <= PREFIX_CROPPING_THRESHOLD) return this

        return "..." + this.substring(startIndex, this.length)
    }

    companion object {
        private const val PREFIX_CROPPING_THRESHOLD = 30
    }
}
