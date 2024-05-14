package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.model.TeamSpace

interface EditableSpaceFilter : SpaceFilter {

    var spaceFilter: SpaceFilter

    fun forCurrentSpace() {
        spaceFilter = CurrentSpaceFilter
    }

    fun noSpaceFilter() {
        spaceFilter = NoSpaceFilter
    }

    fun specificSpace(vararg spaces: TeamSpace) {
        spaceFilter = SpecificSpaceFilter(spaces.toList())
    }
}