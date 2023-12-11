package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteItemGroupMembersService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.sharing.exception.RequestBuilderException
import com.dashlane.sharing.exception.RequestBuilderException.AddItemsGroupToCollectionRequestException
import com.dashlane.sharing.exception.RequestBuilderException.CreateCollectionRequestException
import com.dashlane.sharing.exception.RequestBuilderException.InviteCollectionMembersRequestException
import com.dashlane.sharing.internal.model.GroupToInvite
import com.dashlane.sharing.internal.model.ItemToShare
import com.dashlane.sharing.internal.model.UserToInvite
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharingRequestRepository @Inject constructor(
    private val createItemGroupRequestBuilder: CreateItemGroupRequestBuilder,
    private val inviteItemGroupMembersRequestBuilder: InviteItemGroupMembersRequestBuilder,
    private val createCollectionRequestBuilder: CreateCollectionRequestBuilder,
    private val addItemsGroupsToCollectionRequestBuilder: AddItemsGroupsToCollectionRequestBuilder,
    private val inviteCollectionMembersRequestBuilder: InviteCollectionMembersRequestBuilder
) {

    @Throws(CreateCollectionRequestException::class)
    suspend fun createCollectionRequest(
        collectionName: String,
        teamId: String,
        author: UserToInvite,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>
    ) = createCollectionRequestBuilder.create(
        collectionName = collectionName,
        author = author,
        teamId = teamId,
        users = users,
        groups = groups
    )

    @Throws(InviteCollectionMembersRequestException::class)
    suspend fun createInviteCollectionMembersRequest(
        collection: Collection,
        myUserGroups: List<UserGroup>,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>
    ) = inviteCollectionMembersRequestBuilder.create(
        collection = collection,
        myUserGroups = myUserGroups,
        users = users,
        groups = groups
    )

    @Throws(AddItemsGroupToCollectionRequestException::class)
    suspend fun createAddItemGroupsToCollectionRequest(
        collection: Collection,
        itemGroups: List<ItemGroup>
    ) = addItemsGroupsToCollectionRequestBuilder.create(
        collection = collection,
        itemGroups = itemGroups
    )

    @Throws(RequestBuilderException::class)
    suspend fun createItemGroupRequest(
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        item: ItemToShare,
        itemForEmailing: ItemForEmailing,
        auditLogs: AuditLogDetails?
    ): CreateItemGroupService.Request {
        return createItemGroupRequestBuilder.create(
            users,
            groups,
            item,
            itemForEmailing,
            auditLogs
        )
    }

    @Throws(RequestBuilderException::class)
    suspend fun createInviteItemGroupMembersRequest(
        itemGroup: ItemGroup,
        itemsForEmailing: List<ItemForEmailing>,
        usersUpload: List<UserToInvite>,
        userGroupsUpload: List<GroupToInvite>,
        myUserGroupsAcceptedOrPending: List<UserGroup>,
        auditLogs: AuditLogDetails?
    ): InviteItemGroupMembersService.Request {
        return inviteItemGroupMembersRequestBuilder.create(
            itemGroup,
            itemsForEmailing,
            usersUpload,
            userGroupsUpload,
            myUserGroupsAcceptedOrPending,
            auditLogs
        )
    }
}