package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter

interface SpaceFilter {

    fun getSpacesRestrictions(currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter): Array<out TeamSpace>?
}