package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace



class SpecificSpaceFilter(private vararg val spaces: Teamspace) : SpaceFilter {

    override fun getSpacesRestrictions(teamspaceAccessor: TeamspaceAccessor): Array<out Teamspace> {
        return spaces
    }
}