package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.CombinedTeamspace
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace

object NoSpaceFilter : SpaceFilter {

    override fun getSpacesRestrictions(teamspaceAccessor: TeamspaceAccessor): Array<out Teamspace> {
        return arrayOf(CombinedTeamspace)
    }
}