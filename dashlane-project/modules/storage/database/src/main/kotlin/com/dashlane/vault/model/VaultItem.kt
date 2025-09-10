package com.dashlane.vault.model

import com.dashlane.database.adapter.VaultItemJsonName
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.copy
import com.dashlane.xml.domain.objectType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class VaultItem<out T : SyncObject>(
    @Transient
    val id: Long = 0,
    @Json(name = VaultItemJsonName.FIELD_ID)
    val uid: String,
    @Json(name = "sharingPermission")
    val sharingPermission: String? = null,
    @Json(name = "locallyViewedDate")
    val locallyViewedDate: Instant? = null,
    @Json(name = "locallyUsedCount")
    val locallyUsedCount: Long = 0,
    @Json(name = VaultItemJsonName.FIELD_SYNC)
    val syncState: SyncState = SyncState.MODIFIED,
    @Json(name = "hasDirtySharedField")
    val hasDirtySharedField: Boolean = false,
    @Json(name = "backupDate")
    val backupDate: Instant? = null,
    @Json(name = VaultItemJsonName.FIELD_DATA)
    val syncObject: T,
) {
    val syncObjectType
        get() = syncObject.objectType

    fun isDeleted(): Boolean {
        return syncState == SyncState.DELETED || syncState == SyncState.IN_SYNC_DELETED
    }

    fun isShared(): Boolean {
        return sharingPermission != null
    }

    @Suppress("UNCHECKED_CAST")
    fun copyWithAttrs(block: DataIdentifierAttrsMutable.() -> Unit): VaultItem<T> {
        val dataIdentifierMutable = DataIdentifierAttrsMutable(this.toCommonDataIdentifierAttrs())
        dataIdentifierMutable.apply(block)

        val syncObjectCopy = this.syncObject.copy {
            attachments = dataIdentifierMutable.attachments
            localeFormat = dataIdentifierMutable.formatLang
            spaceId = dataIdentifierMutable.teamSpaceId
            creationDatetime = dataIdentifierMutable.creationDate
            userModificationDatetime = dataIdentifierMutable.userModificationDate
        } as T

        return this.copy(
            uid = dataIdentifierMutable.uid,
            sharingPermission = dataIdentifierMutable.sharingPermission,
            locallyViewedDate = dataIdentifierMutable.locallyViewedDate,
            locallyUsedCount = dataIdentifierMutable.locallyUsedCount,
            syncState = dataIdentifierMutable.syncState,
            hasDirtySharedField = dataIdentifierMutable.hasDirtySharedField,
            backupDate = dataIdentifierMutable.backupDate,
            syncObject = syncObjectCopy,
        )
    }

    companion object {
        operator fun <T : SyncObject> invoke(
            attrs: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
            syncObject: T,
        ): VaultItem<T> {
            return VaultItem(
                uid = syncObject.id ?: attrs.uid,
                sharingPermission = attrs.sharingPermission,
                locallyViewedDate = attrs.locallyViewedDate,
                locallyUsedCount = attrs.locallyUsedCount,
                syncState = attrs.syncState,
                hasDirtySharedField = attrs.hasDirtySharedField,
                backupDate = attrs.backupDate,
                syncObject = syncObject,
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : SyncObject> T.toVaultItem(
    overrideUid: String? = null,
    sharingPermission: String? = null,
    locallyViewedDate: Instant? = null,
    locallyUsedCount: Long = 0,
    syncState: SyncState = SyncState.SYNCED,
    hasDirtySharedField: Boolean = false,
    backupDate: Instant? = null,
): VaultItem<T> {
    
    val newUid = overrideUid ?: this.id ?: generateUniqueIdentifier()

    val syncObject = if (this.id == newUid) {
        this
    } else {
        this.copy {
            id = newUid
        } as T
    }

    return VaultItem(
        uid = newUid,
        sharingPermission = sharingPermission,
        locallyViewedDate = locallyViewedDate,
        locallyUsedCount = locallyUsedCount,
        syncState = syncState,
        hasDirtySharedField = hasDirtySharedField,
        backupDate = backupDate,
        syncObject = syncObject,
    )
}

fun <T : SyncObject> VaultItem<T>.toCommonDataIdentifierAttrs(): CommonDataIdentifierAttrs {
    return CommonDataIdentifierAttrsImpl(
        uid = this.uid,
        sharingPermission = this.sharingPermission,
        locallyViewedDate = this.locallyViewedDate,
        locallyUsedCount = this.locallyUsedCount,
        syncState = this.syncState,
        hasDirtySharedField = this.hasDirtySharedField,
        attachments = this.syncObject.attachments.takeIf { it.isNotSemanticallyNull() },
        formatLang = syncObject.localeFormat,
        teamSpaceId = this.syncObject.spaceId,
        creationDate = this.syncObject.creationDatetime,
        userModificationDate = this.syncObject.userModificationDatetime,
        backupDate = this.backupDate,
    )
}

fun CharSequence?.isNotSemanticallyNull() = !isSemanticallyNull()

fun CharSequence?.isSemanticallyNull() = isNullOrEmpty() || isValueNull()

fun CharSequence?.isValueNull() = this?.trim() == "null"