package com.dashlane.sharing.internal.builder.request

import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.AuditLogDetails
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateItemGroupService
import com.dashlane.server.api.endpoints.sharinguserdevice.CreateMultipleItemGroupsService
import com.dashlane.server.api.endpoints.sharinguserdevice.InviteMultipleItemGroupMembersService
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
    private val createMultipleItemGroupsRequestBuilder: CreateMultipleItemGroupsRequestBuilder,
    private val inviteMultipleItemGroupMembersRequestBuilder: InviteMultipleItemGroupMembersRequestBuilder,
    private val createCollectionRequestBuilder: CreateCollectionRequestBuilder,
    private val addItemsGroupsToCollectionRequestBuilder: AddItemsGroupsToCollectionRequestBuilder,
    private val inviteCollectionMembersRequestBuilder: InviteCollectionMembersRequestBuilder,
) {
    @Throws(CreateCollectionRequestException::class)
    suspend fun createCollectionRequest(
        collectionName: String,
        teamId: String,
        author: UserToInvite,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
    ) = createCollectionRequestBuilder.create(
        collectionName = collectionName,
        author = author,
        teamId = teamId,
        users = users,
        groups = groups,
    )

    @Throws(InviteCollectionMembersRequestException::class)
    suspend fun createInviteCollectionMembersRequest(
        username: String,
        collection: Collection,
        myUserGroups: List<UserGroup>,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
    ) = inviteCollectionMembersRequestBuilder.create(
        username = username,
        collection = collection,
        myUserGroups = myUserGroups,
        users = users,
        groups = groups,
    )

    @Throws(AddItemsGroupToCollectionRequestException::class)
    suspend fun createAddItemGroupsToCollectionRequest(
        username: String,
        authorization: Authorization.User,
        collection: Collection,
        itemGroups: List<ItemGroup>,
        userGroups: List<UserGroup>,
    ) = addItemsGroupsToCollectionRequestBuilder.create(
        username = username,
        authorization = authorization,
        collection = collection,
        itemGroups = itemGroups,
        userGroups = userGroups,
    )

    @Throws(RequestBuilderException::class)
    suspend fun createItemGroupRequest(
        username: String,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        item: ItemToShare,
        itemForEmailing: ItemForEmailing,
        auditLogs: AuditLogDetails?,
    ): CreateItemGroupService.Request {
        return createItemGroupRequestBuilder.create(
            username,
            users,
            groups,
            item,
            itemForEmailing,
            auditLogs,
        )
    }

    @Throws(RequestBuilderException::class)
    suspend fun createMultipleItemGroupsRequest(
        username: String,
        users: List<UserToInvite>,
        groups: List<GroupToInvite>,
        items: List<Triple<ItemToShare, ItemForEmailing, AuditLogDetails?>>,
    ): CreateMultipleItemGroupsService.Request {
        return createMultipleItemGroupsRequestBuilder.create(
            username,
            users,
            groups,
            items,
        )
    }

    @Throws(RequestBuilderException::class)
    suspend fun createMultipleInviteItemGroupMembersRequest(
        username: String,
        itemGroups: List<Triple<ItemGroup, List<ItemForEmailing>, AuditLogDetails?>>,
        usersUpload: List<UserToInvite>,
        userGroupsUpload: List<GroupToInvite>,
        myUserGroupsAcceptedOrPending: List<UserGroup>,
    ): InviteMultipleItemGroupMembersService.Request {
        return inviteMultipleItemGroupMembersRequestBuilder.create(
            username,
            itemGroups,
            usersUpload,
            userGroupsUpload,
            myUserGroupsAcceptedOrPending,
        )
    }
}