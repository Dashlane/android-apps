package com.dashlane.vault.model

import com.dashlane.xml.domain.utils.Country
import java.time.Instant

interface DataIdentifierAttrs {
    val id: Int
    val uid: String
    val attachments: String?
    val formatLang: Country?
    val syncState: SyncState
    val hasDirtySharedField: Boolean
    val sharingPermission: String?
    val teamSpaceId: String?

    val creationDate: Instant?
    val userModificationDate: Instant?
    val locallyViewedDate: Instant?
    val locallyUsedCount: Long
    val backupDate: Instant?

    val isDeleted: Boolean
        get() = syncState == SyncState.DELETED || syncState == SyncState.IN_SYNC_DELETED
}