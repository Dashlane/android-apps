package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun createAuthCategory(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    name: String? = null
): VaultItem<SyncObject.AuthCategory> {
    return dataIdentifier.toVaultItem(
        SyncObject.AuthCategory {
            this.categoryName = name

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

val SyncObject.AuthCategory.Companion.alphabeticalComparator
    get() = compareBy(SyncObject.AuthCategory::categoryName)

fun VaultItem<SyncObject.AuthCategory>.copySyncObject(builder: SyncObject.AuthCategory.Builder.() -> Unit = {}):
        VaultItem<SyncObject.AuthCategory> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
