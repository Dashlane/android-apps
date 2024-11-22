package com.dashlane.item.v3.data

import com.dashlane.teamspaces.model.TeamSpace
import java.time.Instant

data class CommonData(
    val id: String = "",
    val name: String = "",
    val isShared: Boolean = false,
    val isEditable: Boolean = false,
    val isCopyActionAllowed: Boolean = true,
    val canDelete: Boolean = false,
    val sharingCount: FormData.SharingCount = FormData.SharingCount(),
    val collections: List<CollectionData>? = null,
    val created: Instant? = null,

    val updated: Instant? = null,

    val isForcedSpace: Boolean = false,

    val space: TeamSpace? = null,

    val availableSpaces: List<TeamSpace> = emptyList(),

    val isSharedWithLimitedRight: Boolean = false,

    val attachmentCount: Int = 0
)