package com.dashlane.ui.screens.fragments.userdata.sharing.center

import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.vault.summary.SummaryObject

interface SharingDataProvider {
    suspend fun getItemGroups(): List<ItemGroup>
    suspend fun getUserGroups(): List<UserGroup>
    suspend fun getUserGroupsAccepted(login: String): List<UserGroup>
    suspend fun getCollections(): List<Collection>
    suspend fun getCollections(itemId: String, needsAdminRights: Boolean): List<Collection>
    suspend fun getAcceptedCollections(needsAdminRights: Boolean): List<Collection>
    suspend fun getAcceptedCollections(
        userId: String,
        needsAdminRights: Boolean
    ): List<Collection>

    suspend fun getAcceptedCollectionsForGroup(userGroupId: String): List<Collection>

    suspend fun getAcceptedCollectionsItems(uuid: String): List<SummaryObject>
    suspend fun getTeamLogins(): List<String>
    suspend fun isCollectionShareAllowed(collection: Collection): Boolean
    fun getSummaryObject(itemId: String): SummaryObject?
    suspend fun acceptItemGroupInvite(
        itemGroup: ItemGroup,
        summaryObject: SummaryObject,
        handleConflict: Boolean
    )

    suspend fun acceptUserGroupInvite(
        userGroup: UserGroup,
        handleConflict: Boolean
    )

    suspend fun acceptCollectionInvite(
        collection: Collection,
        handleConflict: Boolean
    )

    suspend fun declineItemGroupInvite(
        itemGroup: ItemGroup,
        summaryObject: SummaryObject,
        handleConflict: Boolean,
        loggerAction: String? = null
    )

    suspend fun declineUserGroupInvite(userGroup: UserGroup, handleConflict: Boolean)

    suspend fun declineCollectionInvite(collection: Collection, handleConflict: Boolean)

    suspend fun resendInvite(itemGroup: ItemGroup, memberLogin: String)

    suspend fun cancelInvitation(
        itemGroup: ItemGroup,
        userIds: List<String>,
        handleConflict: Boolean = true
    ) = cancelInvitationUsersAndUserGroups(
        itemGroup,
        userIds = userIds,
        handleConflict = handleConflict
    )

    suspend fun cancelInvitationUserGroups(
        itemGroup: ItemGroup,
        userGroupIds: List<String>,
        handleConflict: Boolean = true
    ) = cancelInvitationUsersAndUserGroups(itemGroup, userGroupIds = userGroupIds, handleConflict = handleConflict)

    suspend fun cancelInvitationUsersAndUserGroups(
        itemGroup: ItemGroup,
        userIds: List<String>? = null,
        userGroupIds: List<String>? = null,
        handleConflict: Boolean
    )

    suspend fun updateItemGroupMember(
        itemGroup: ItemGroup,
        newPermission: Permission,
        userId: String,
        handleConflict: Boolean = true
    )

    suspend fun updateItemGroupMemberUserGroup(
        itemGroup: ItemGroup,
        newPermission: Permission,
        userGroupId: String,
        handleConflict: Boolean = true
    )
}