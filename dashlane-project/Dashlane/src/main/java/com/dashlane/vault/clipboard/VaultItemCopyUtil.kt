package com.dashlane.vault.clipboard

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.toHighlight

object VaultItemCopyUtil {
    private val vaultItemClipboard
        get() = SingletonProvider.getComponent().vaultItemClipboard

    fun handleCopy(summaryObject: SummaryObject, copyField: CopyField) {
        vaultItemClipboard.handleCopy(summaryObject, copyField, updateLocalUsage = true, updateFrequentSearch = true)
    }

    fun handleCopy(
        item: SummaryObject,
        copyField: CopyField,
        itemListContext: ItemListContext
    ) {
        vaultItemClipboard.handleCopy(
            item,
            copyField,
            true,
            itemListContext.container == ItemListContext.Container.SEARCH,
            itemListContext.section.toHighlight(),
            itemListContext.positionInContainerSection.toDouble(),
            itemListContext.sectionCount
        )
    }

    fun hasContent(item: SummaryObject, copyField: CopyField): Boolean =
        vaultItemClipboard.hasContent(item, copyField)
}