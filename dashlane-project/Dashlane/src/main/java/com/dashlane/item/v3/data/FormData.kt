package com.dashlane.item.v3.data

import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject

interface FormData {
    abstract class Builder<T : FormData> {
        var teamSpace: TeamSpace? = null

        var availableSpaces: List<TeamSpace> = emptyList()

        var isEditable: Boolean? = null

        var isCopyActionAllowed: Boolean = true

        var sharingCount: SharingCount? = null

        var canDelete: Boolean? = null

        abstract fun build(
            initialSummaryObject: SummaryObject,
            state: ItemEditState<T>
        ): Data<T>
    }

    data class SharingCount(val userCount: Int, val groupCount: Int) {
        constructor(sharingCount: Pair<Int, Int>) : this(sharingCount.first, sharingCount.second)
        constructor() : this(0, 0)
    }
}