package com.dashlane.sharing

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.sharing.internal.builder.request.UpdateItemGroupMembersRequestBuilder
import com.dashlane.sharing.model.getUsersToUpdate
import javax.inject.Inject

class SharingItemGroupInvitePublicKeyUser @Inject constructor(
    private val updateItemGroupMembersService: UpdateItemGroupMembersService,
    private val updateItemGroupMembersRequestBuilder: UpdateItemGroupMembersRequestBuilder,
) {
    suspend fun execute(
        username: String,
        authorization: Authorization.User,
        itemGroupsAccepted: List<ItemGroup>,
        myUserGroupsAcceptedOrPending: List<UserGroup>,
        usersToRequest: Map<String, List<UserDownload>>,
        users: List<GetUsersPublicKeyService.Data.Data>,
    ): List<ItemGroup> {
        return itemGroupsAccepted.mapNotNull { itemGroup ->
            
            val userToUpdates =
                usersToRequest[itemGroup.groupId]?.getUsersToUpdate(users) ?: return emptyList()
            val request = updateItemGroupMembersRequestBuilder.build(
                username,
                itemGroup,
                myUserGroupsAcceptedOrPending,
                userToUpdates,
            )
            updateItemGroupMembersService.execute(
                authorization,
                request,
            ).data.itemGroups
        }.flatten()
    }
}