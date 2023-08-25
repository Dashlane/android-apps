package com.dashlane.core.sharing

import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateUserGroupUsersService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.sharing.internal.builder.request.UpdateUserGroupUsersRequestBuilder
import com.dashlane.ui.screens.fragments.userdata.sharing.getUsersToUpdate
import javax.inject.Inject

class SharingUserGroupInvitePublicKeyUser @Inject constructor(
    private val updateUserGroupUsersService: UpdateUserGroupUsersService,
    private val updateUserGroupUsersRequestBuilder: UpdateUserGroupUsersRequestBuilder
) {

    suspend fun execute(
        session: Session,
        userGroupsAccepted: List<UserGroup>,
        usersToRequest: Map<String, List<UserDownload>>,
        users: List<GetUsersPublicKeyService.Data.Data>
    ): List<UserGroup> {
        val authorization = session.authorization
        return userGroupsAccepted.mapNotNull { userGroup ->
            val userToUpdates = usersToRequest[userGroup.groupId]?.getUsersToUpdate(users)
                ?: return emptyList()
            val request = updateUserGroupUsersRequestBuilder.build(userGroup, userToUpdates)
            updateUserGroupUsersService.execute(
                authorization,
                request
            ).data.userGroups
        }.flatten()
    }
}
