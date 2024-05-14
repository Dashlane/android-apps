package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.manager.DataIdentifierSpaceCategorization
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter

object NoRestrictionSpaceFilter : SpaceFilter {

    override fun getSpacesRestrictions(currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter): Array<out TeamSpace>? = null
}