package com.dashlane.vault.model

import com.dashlane.util.toItemUuidOrNull
import com.dashlane.util.tryOrNull
import com.dashlane.xml.domain.SyncObject
import java.util.UUID

val VaultItem<*>.hasBeenSaved: Boolean
    get() = id != 0L || syncObject.creationDatetime != null

fun VaultItem<*>.uidAsUuidOrNull(): UUID? =
    tryOrNull { uid.toItemUuidOrNull() }

fun <T : SyncObject> DataIdentifierAttrs.toVaultItem(syncObject: T): VaultItem<T> {
    return VaultItem(
        id = id.toLong(),
        uid = uid,
        sharingPermission = sharingPermission,
        locallyViewedDate = locallyViewedDate,
        locallyUsedCount = locallyUsedCount,
        syncState = syncState,
        hasDirtySharedField = hasDirtySharedField,
        syncObject = syncObject
    )
}

fun SyncObject.Builder.setCommonDataIdentifierAttrs(dataIdentifier: CommonDataIdentifierAttrs) {
    this.id = dataIdentifier.uid
    this.anonId = dataIdentifier.anonymousUID
    this.attachments = dataIdentifier.attachments
    this.localeFormat = dataIdentifier.formatLang
    this.spaceId = dataIdentifier.teamSpaceId
    this.creationDatetime = dataIdentifier.creationDate
    this.userModificationDatetime = dataIdentifier.userModificationDate
}

@Suppress("UNCHECKED_CAST")
fun <T : SyncObject> VaultItem<*>.asVaultItemOfClassOrNull(clazz: Class<T>): VaultItem<T>? =
    if (clazz.isInstance(syncObject)) this as VaultItem<T> else null
