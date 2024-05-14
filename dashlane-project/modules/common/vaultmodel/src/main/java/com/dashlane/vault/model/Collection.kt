package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

const val COLLECTION_NAME_MAX_LENGTH = 40

fun createCollection(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    name: String? = null,
    vaultItems: List<SyncObject.Collection.VaultItems>
) = dataIdentifier.toVaultItem(
    SyncObject.Collection {
        this.name = name
        this.vaultItems = vaultItems

        this.setCommonDataIdentifierAttrs(dataIdentifier)
    }
)

fun VaultItem<SyncObject.Collection>.copySyncObject(builder: SyncObject.Collection.Builder.() -> Unit = {}):
        VaultItem<SyncObject.Collection> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}

fun VaultItem<SyncObject>.toCollectionVaultItem(): SyncObject.Collection.VaultItems =
    SyncObject.Collection.VaultItems.Builder().apply {
        id = uid
        type = toCollectionDataType()
    }.build()

fun VaultItem<SyncObject>.toCollectionDataType(): SyncObject.CollectionDataType? =
    SyncObject.CollectionDataType.values().firstOrNull { syncObjectType.xmlObjectName == it.value }

fun String.toSanitizedCollectionName() =
    lines().joinToString(separator = "").trim().take(COLLECTION_NAME_MAX_LENGTH).trim()