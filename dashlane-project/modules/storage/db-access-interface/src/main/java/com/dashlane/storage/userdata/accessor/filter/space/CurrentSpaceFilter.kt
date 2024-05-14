package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter

object CurrentSpaceFilter : SpaceFilter {

    override fun getSpacesRestrictions(currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter): Array<out TeamSpace> {
        val currentSpace = currentTeamSpaceUiFilter.currentFilter.teamSpace

        return when {
            
            (currentSpace as? TeamSpace.Business.Current)?.hasPersonalSpace == false -> arrayOf(TeamSpace.Personal, currentSpace)
            else -> arrayOf(currentSpace)
        }
    }
}