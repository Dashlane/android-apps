package com.dashlane.storage.userdata.accessor.filter.space

import com.dashlane.teamspaces.manager.DataIdentifierSpaceCategorization
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace



object NoRestrictionSpaceFilter : SpaceFilter {

    override fun getSpacesRestrictions(teamspaceAccessor: TeamspaceAccessor): Array<out Teamspace>? = null
}