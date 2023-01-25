package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun createPersonalWebsite(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    website: String? = null,
    name: String? = null
): VaultItem<SyncObject.PersonalWebsite> {
    return dataIdentifier.toVaultItem(
        SyncObject.PersonalWebsite {
            this.website = website
            this.name = name

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.PersonalWebsite>.copySyncObject(builder: SyncObject.PersonalWebsite.Builder.() -> Unit = {}):
        VaultItem<SyncObject.PersonalWebsite> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
