package com.dashlane.vault.model

import com.dashlane.xml.domain.utils.Country
import java.time.Instant

interface CommonDataIdentifierAttrs : DataIdentifierAttrs {
    override val id: Int
    override val uid: String
    override val attachments: String?
    override val formatLang: Country?
    override val syncState: SyncState
    override val hasDirtySharedField: Boolean
    override val sharingPermission: String?
    override val teamSpaceId: String?

    override val creationDate: Instant?
    override val userModificationDate: Instant?
    override val locallyViewedDate: Instant?
    override val locallyUsedCount: Long
    override val backupDate: Instant?
}