package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter

object NoSpaceFilter : SpaceFilter {

    override fun getSpacesRestrictions(currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter): Array<out TeamSpace> {
        return arrayOf(TeamSpace.Combined)
    }
}