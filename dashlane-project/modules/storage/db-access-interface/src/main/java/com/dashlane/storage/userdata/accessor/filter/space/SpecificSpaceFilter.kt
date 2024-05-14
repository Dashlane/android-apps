package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter

data class SpecificSpaceFilter(private val spaces: List<TeamSpace>) : SpaceFilter {

    override fun getSpacesRestrictions(currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter): Array<out TeamSpace> {
        return spaces.toTypedArray()
    }
}