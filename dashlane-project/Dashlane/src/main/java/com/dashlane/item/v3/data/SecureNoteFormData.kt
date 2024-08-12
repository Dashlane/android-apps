package com.dashlane.item.v3.data

import com.dashlane.item.v3.data.FormData.SharingCount
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject
import java.time.Instant

data class SecureNoteFormData(
    override val id: String = "",
    override val name: String = "",
    override val isShared: Boolean = false,
    override val isEditable: Boolean = true,
    override val isCopyActionAllowed: Boolean = true,
    override val canDelete: Boolean = false,
    override val sharingCount: SharingCount = SharingCount(),
    override val collections: List<CollectionData> = emptyList(),
    override val space: TeamSpace? = null,
    override val availableSpaces: List<TeamSpace> = emptyList(),
    override val isForcedSpace: Boolean = false,
    override val created: Instant? = null,
    override val updated: Instant? = null,
    val note: String? = null
    
) : FormData()

internal fun SummaryObject.SecureNote.toSecureNoteFormData(
    availableSpaces: List<TeamSpace>,
    isEditable: Boolean?,
    canDelete: Boolean?,
    sharingCount: SharingCount?,
    teamSpace: TeamSpace?
) = SecureNoteFormData(
    id = this.id,
    name = this.title ?: "",
    isShared = this.isShared,
    isEditable = isEditable ?: false,
    canDelete = canDelete ?: false,
    sharingCount = sharingCount ?: SharingCount(),
    
    space = teamSpace,
    availableSpaces = availableSpaces,
    created = this.creationDatetime,
    updated = this.userModificationDatetime
)