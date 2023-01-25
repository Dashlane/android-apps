package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.endpoints.sharinguserdevice.CreateItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharingRequestRepository @Inject constructor(
    private val createItemGroupRequestBuilder: CreateItemGroupRequestBuilder,
    private val inviteItemGroupMembersRequestBuilder: InviteItemGroupMembersRequestBuilder
) {

    @Throws(RequestBuilderException::class)
    suspend fun createItemGroupRequest(
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        item: ItemToShare,
        itemForEmailing: ItemForEmailing
    ): CreateItemGroupService.Request {
        return createItemGroupRequestBuilder.create(users, groups, item, itemForEmailing)
    }

    @Throws(RequestBuilderException::class)
    suspend fun createInviteItemGroupMembersRequest(
        itemGroup: ItemGroup,
        itemsForEmailing: List<ItemForEmailing>,
        usersUpload: List<UserToInvite>,
        userGroupsUpload: List<GroupToInvite>,
        myUserGroupsAcceptedOrPending: List<UserGroup>
    ): InviteItemGroupMembersService.Request {
        return inviteItemGroupMembersRequestBuilder.create(
            itemGroup,
            itemsForEmailing,
            usersUpload,
            userGroupsUpload,
            myUserGroupsAcceptedOrPending
        )
    }
}