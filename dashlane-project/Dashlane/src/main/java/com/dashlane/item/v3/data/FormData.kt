package com.dashlane.item.v3.data

import com.dashlane.item.v3.viewmodels.State
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject
import java.time.Instant

abstract class FormData {
    abstract val id: String

    abstract val name: String

    abstract val isShared: Boolean

    abstract val isEditable: Boolean

    abstract val isCopyActionAllowed: Boolean

    abstract val canDelete: Boolean

    abstract val sharingCount: SharingCount

    abstract val collections: List<CollectionData>

    abstract val created: Instant?

    abstract val updated: Instant?

    abstract val isForcedSpace: Boolean

    abstract val space: TeamSpace?

    abstract val availableSpaces: List<TeamSpace>

    abstract class Builder {
        var teamSpace: TeamSpace? = null

        var availableSpaces: List<TeamSpace> = emptyList()

        var isEditable: Boolean? = null

        var isCopyActionAllowed: Boolean = true

        var sharingCount: SharingCount? = null

        var canDelete: Boolean? = null

        abstract fun build(
            initialSummaryObject: SummaryObject,
            state: State
        ): FormData
    }

    data class SharingCount(val userCount: Int, val groupCount: Int) {
        constructor(sharingCount: Pair<Int, Int>) : this(sharingCount.first, sharingCount.second)
        constructor() : this(0, 0)
    }
}