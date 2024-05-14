package com.dashlane.loaders.datalists

import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class SharingUserDataUtils @Inject constructor(
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>
) {

    fun isMemberOfUserGroupTeam(userGroup: UserGroup): Boolean {
        val userGroupTeamId = userGroup.teamId ?: return true
        return teamSpaceAccessorProvider.get()?.get(userGroupTeamId.toString()) is TeamSpace.Business.Current
    }
}