package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace



interface SpaceFilter {

    fun getSpacesRestrictions(teamspaceAccessor: TeamspaceAccessor): Array<out Teamspace>?
}