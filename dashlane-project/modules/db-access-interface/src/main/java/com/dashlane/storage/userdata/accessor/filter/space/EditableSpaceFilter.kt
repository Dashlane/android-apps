package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.model.Teamspace



interface EditableSpaceFilter : SpaceFilter {

    var spaceFilter: SpaceFilter

    fun forCurrentSpace() {
        spaceFilter = CurrentSpaceFilter
    }

    fun noSpaceFilter() {
        spaceFilter = NoSpaceFilter
    }

    fun specificSpace(vararg spaces: Teamspace) {
        spaceFilter = SpecificSpaceFilter(*spaces)
    }
}