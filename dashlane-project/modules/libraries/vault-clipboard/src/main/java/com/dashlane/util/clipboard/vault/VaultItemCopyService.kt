package com.dashlane.util.clipboard.vault

import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.summary.SummaryObject

interface VaultItemCopyService {
    fun handleCopy(notificationId: String, content: String, copyField: CopyField): Boolean

    fun handleCopy(itemId: String, copyField: CopyField): Boolean

    fun handleCopy(
        item: SummaryObject,
        copyField: CopyField,
        itemListContext: ItemListContext,
    ): Boolean

    fun handleCopy(
        item: SummaryObject,
        copyField: CopyField,
        updateFrequentSearch: Boolean = false,
        highlight: Highlight? = null,
        index: Double? = null,
        totalCount: Int? = null,
    ): Boolean

    fun hasContent(item: SummaryObject, copyField: CopyField): Boolean
}