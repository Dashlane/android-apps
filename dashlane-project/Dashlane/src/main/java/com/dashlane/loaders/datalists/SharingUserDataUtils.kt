package com.dashlane.loaders.datalists

import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class SharingUserDataUtils @Inject constructor(
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>
) {

    fun isMemberOfUserGroupTeam(userGroup: UserGroup): Boolean {
        val userGroupTeamId = userGroup.teamId ?: return true
        return teamspaceAccessorProvider.get()?.get(userGroupTeamId.toString())?.status == Teamspace.Status.ACCEPTED
    }
}