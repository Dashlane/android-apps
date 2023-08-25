package com.dashlane.loaders.datalists

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.teamspaces.model.Teamspace

object SharingUserDataUtils {

    fun isMemberOfUserGroupTeam(userGroup: UserGroup): Boolean {
        val userGroupTeamId = userGroup.teamId ?: return true
        return SingletonProvider.getComponent().teamspaceAccessor
            .get()?.get(userGroupTeamId.toString())?.status == Teamspace.Status.ACCEPTED
    }
}