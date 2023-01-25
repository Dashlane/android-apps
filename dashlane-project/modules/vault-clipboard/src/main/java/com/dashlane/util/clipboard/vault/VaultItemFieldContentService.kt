package com.dashlane.util.clipboard.vault

import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject



interface VaultItemFieldContentService {
    fun hasContent(item: SummaryObject, copyField: CopyField): Boolean
    fun hasContent(vaultItem: VaultItem<*>, copyField: CopyField): Boolean
    fun getContent(item: SummaryObject, copyField: CopyField): String?
    fun getContent(vaultItem: VaultItem<*>, copyField: CopyField): String?
}