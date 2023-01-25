package com.dashlane.vault.model

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

fun createSecureNoteCategory(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    title: String? = null
): VaultItem<SyncObject.SecureNoteCategory> {
    return dataIdentifier.toVaultItem(
        SyncObject.SecureNoteCategory {
            this.categoryName = title

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

val SyncObject.SecureNoteCategory.Companion.alphabeticalComparator get() = compareBy(SummaryObject.SecureNoteCategory::categoryName)

fun VaultItem<SyncObject.SecureNoteCategory>.copySyncObject(builder: SyncObject.SecureNoteCategory.Builder.() -> Unit = {}):
        VaultItem<SyncObject.SecureNoteCategory> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
