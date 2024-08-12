package com.dashlane.item.v3.data

import com.dashlane.teamspaces.model.TeamSpace
import java.time.Instant

class LoadingFormData(override val id: String, val firstLoad: Boolean) : FormData() {
    override val name = ""
    override val isShared = false
    override val isEditable: Boolean = false
    override val isCopyActionAllowed: Boolean = false
    override val canDelete: Boolean = false
    override val sharingCount: SharingCount = SharingCount()
    override val collections: List<CollectionData> = emptyList()
    override val created: Instant? = null
    override val updated: Instant? = null
    override val space: TeamSpace? = null
    override val availableSpaces: List<TeamSpace> = emptyList()
    override val isForcedSpace: Boolean = false
}