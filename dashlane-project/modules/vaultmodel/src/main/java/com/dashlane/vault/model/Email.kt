package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun createEmail(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    type: SyncObject.Email.Type? = null,
    emailName: String? = null,
    emailAddress: String? = null
): VaultItem<SyncObject.Email> {
    return dataIdentifier.toVaultItem(
        SyncObject.Email {
            this.type = type
            this.emailName = emailName
            this.email = emailAddress

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        }
    )
}

fun VaultItem<SyncObject.Email>.copySyncObject(builder: SyncObject.Email.Builder.() -> Unit = {}):
        VaultItem<SyncObject.Email> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
