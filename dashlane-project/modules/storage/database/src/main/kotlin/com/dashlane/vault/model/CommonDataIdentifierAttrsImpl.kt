package com.dashlane.vault.model

import com.dashlane.xml.domain.utils.Country
import java.time.Instant
import java.util.UUID

data class CommonDataIdentifierAttrsImpl(
    override val uid: String = generateUniqueIdentifier(),
    override val id: Int = 0,
    override val attachments: String? = null,
    override val formatLang: Country? = Country.UnitedStates,
    override val syncState: SyncState = SyncState.SYNCED,
    override val hasDirtySharedField: Boolean = false,
    override val sharingPermission: String? = null,
    override val locallyViewedDate: Instant? = null,
    override val locallyUsedCount: Long = 0,
    override val creationDate: Instant? = null,
    override val userModificationDate: Instant? = null,
    override val teamSpaceId: String? = null,
    override val backupDate: Instant? = null,
) : CommonDataIdentifierAttrs

fun generateUniqueIdentifier() = UUID.randomUUID().let { "{$it}".uppercase() }