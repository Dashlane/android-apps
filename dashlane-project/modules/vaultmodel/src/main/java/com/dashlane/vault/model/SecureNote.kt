package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun createSecureNote(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    title: String? = null,
    type: SyncObject.SecureNoteType = SyncObject.SecureNoteType.BLUE,
    category: String? = null,
    content: String? = null,
    isSecured: Boolean = false
): VaultItem<SyncObject.SecureNote> {
    return dataIdentifier.toVaultItem(
        SyncObject.SecureNote {
            this.title = title
            this.type = type
            this.category = category
            this.content = content
            this.secured = isSecured

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.SecureNote>.copySyncObject(builder: SyncObject.SecureNote.Builder.() -> Unit = {}):
        VaultItem<SyncObject.SecureNote> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
