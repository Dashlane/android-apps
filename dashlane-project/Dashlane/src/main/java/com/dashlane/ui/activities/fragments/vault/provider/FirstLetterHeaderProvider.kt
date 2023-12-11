package com.dashlane.ui.activities.fragments.vault.provider

import android.content.Context
import com.dashlane.R
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.activities.fragments.vault.VaultItemViewTypeProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.ViewTypeProvider
import com.dashlane.util.tryOrNull
import com.dashlane.vault.util.IdentityNameHolderService
import com.dashlane.vault.util.getComparableField
import java.util.Locale

object FirstLetterHeaderProvider : HeaderProvider {
    override fun getHeaderFor(
        context: Context,
        viewTypeProvider: ViewTypeProvider,
        identityNameHolderService: IdentityNameHolderService
    ): String {
        val text = getTitleForHeader(context, viewTypeProvider, identityNameHolderService)
        val emptyTextCategory = "..."
        val firstLetter = tryOrNull { text?.substring(0, 1)?.uppercase(Locale.US) }?.takeUnless { it.isEmpty() }
            ?: " "

        return when (firstLetter[0]) {
            ' ' -> emptyTextCategory
            in '0'..'9' -> context.getString(R.string.alphabet_indexer_grouping_numbers_0_9)
            else -> firstLetter
        }
    }

    private fun getTitleForHeader(
        context: Context,
        viewTypeProvider: ViewTypeProvider,
        identityNameHolderService: IdentityNameHolderService
    ): String? {
        return when (viewTypeProvider) {
            is VaultItemViewTypeProvider -> viewTypeProvider.summaryObject.getComparableField(identityNameHolderService)
            is VaultItemWrapper<*> -> viewTypeProvider.getTitle(context).text
            else -> null
        }
    }
}