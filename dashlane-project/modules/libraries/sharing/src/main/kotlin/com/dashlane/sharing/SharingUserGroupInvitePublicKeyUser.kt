package com.dashlane.sharing

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateUserGroupUsersService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.sharing.internal.builder.request.UpdateUserGroupUsersRequestBuilder
import com.dashlane.sharing.model.getUsersToUpdate
import javax.inject.Inject

class SharingUserGroupInvitePublicKeyUser @Inject constructor(
    private val updateUserGroupUsersService: UpdateUserGroupUsersService,
    private val updateUserGroupUsersRequestBuilder: UpdateUserGroupUsersRequestBuilder,
) {
    suspend fun execute(
        username: String,
        authorization: Authorization.User,
        userGroupsAccepted: List<UserGroup>,
        usersToRequest: Map<String, List<UserDownload>>,
        users: List<GetUsersPublicKeyService.Data.Data>,
    ): List<UserGroup> {
        return userGroupsAccepted.mapNotNull { userGroup ->
            val userToUpdates = usersToRequest[userGroup.groupId]?.getUsersToUpdate(users)
                ?: return emptyList()
            val request = updateUserGroupUsersRequestBuilder.build(username, userGroup, userToUpdates)
            updateUserGroupUsersService.execute(
                authorization,
                request,
            ).data.userGroups
        }.flatten()
    }
}