package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace



object CurrentSpaceFilter : SpaceFilter {

    override fun getSpacesRestrictions(teamspaceAccessor: TeamspaceAccessor): Array<out Teamspace> {
        return arrayOf(teamspaceAccessor.current ?: teamspaceAccessor.combinedTeamspace)
    }
}