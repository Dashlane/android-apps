package com.dashlane.core.sharing

import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UpdateItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.sharing.internal.builder.request.UpdateItemGroupMembersRequestBuilder
import com.dashlane.ui.screens.fragments.userdata.sharing.getUsersToUpdate
import javax.inject.Inject

class SharingItemGroupInvitePublicKeyUser @Inject constructor(
    private val updateItemGroupMembersService: UpdateItemGroupMembersService,
    private val updateItemGroupMembersRequestBuilder: UpdateItemGroupMembersRequestBuilder
) {

    suspend fun execute(
        session: Session,
        itemGroupsAccepted: List<ItemGroup>,
        myUserGroupsAcceptedOrPending: List<UserGroup>,
        usersToRequest: Map<String, List<UserDownload>>,
        users: List<GetUsersPublicKeyService.Data.Data>
    ): List<ItemGroup> {
        val authorization = session.authorization

        return itemGroupsAccepted.mapNotNull { itemGroup ->
            
            val userToUpdates =
                usersToRequest[itemGroup.groupId]?.getUsersToUpdate(users) ?: return emptyList()
            val request = updateItemGroupMembersRequestBuilder.build(
                itemGroup,
                myUserGroupsAcceptedOrPending,
                userToUpdates
            )
            updateItemGroupMembersService.execute(
                authorization,
                request
            ).data.itemGroups
        }.flatten()
    }
}
