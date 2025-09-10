package com.dashlane.item.v3.data

import com.dashlane.item.v3.util.Field
import com.dashlane.item.v3.viewmodels.Data
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

        abstract suspend fun build(
            initialSummaryObject: SummaryObject,
            isNew: Boolean,
        ): Data<T>

        open fun buildHiddenFields(
            data: T,
            commonData: CommonData,
        ): Set<Field> = emptySet()
    }

    data class SharingCount(val userCount: Int, val groupCount: Int) {
        constructor(sharingCount: Pair<Int, Int>) : this(sharingCount.first, sharingCount.second)
        constructor() : this(0, 0)
    }
}