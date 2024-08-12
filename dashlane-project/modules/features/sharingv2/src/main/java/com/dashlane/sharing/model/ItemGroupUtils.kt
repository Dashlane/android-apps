package com.dashlane.sharing.model

import androidx.annotation.VisibleForTesting
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.CollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.server.api.endpoints.sharinguserdevice.Status
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroupMember

val ItemGroup.countAdminUsers: Int
    get() = users?.count {
        Status.ACCEPTED == it.status && Permission.ADMIN == it.permission
    } ?: 0

val ItemGroup.countAdminUserGroups: Int
    get() = groups?.count {
        Status.ACCEPTED == it.status && Permission.ADMIN == it.permission
    } ?: 0

fun ItemGroup.getUser(userId: String): UserDownload? = users?.find { it.userId == userId }

fun ItemGroup.getUserGroupMember(userGroupId: String): UserGroupMember? =
    groups?.find { it.groupId == userGroupId }

fun ItemGroup.getCollectionDownload(collectionId: String): CollectionDownload? =
    collections?.find { it.uuid == collectionId }

fun ItemGroup.getUserGroupMembers(userGroups: List<UserGroup>): List<UserGroupMember> =
    getUserGroupMembers(userGroups, false)

fun ItemGroup.getCollectionDownloads(collections: List<Collection>): List<CollectionDownload> =
    getCollectionDownloads(collections, false)

fun ItemGroup.getUserGroupMembers(
    userGroups: List<UserGroup>,
    onlyAcceptedOrPending: Boolean
): List<UserGroupMember> {
    val groupIds = userGroups.map { it.groupId }
    return groups?.filter {
        it.groupId in groupIds
    }?.let { result ->
        if (onlyAcceptedOrPending) {
            result.filter { it.isAcceptedOrPending }
        } else {
            result
        }
    } ?: emptyList()
}

fun ItemGroup.getCollectionMembers(
    myCollections: List<Collection>,
    onlyAcceptedOrPending: Boolean
): List<CollectionDownload> {
    val myCollectionIds = myCollections.map { it.uuid }
    return collections?.filter {
        it.uuid in myCollectionIds
    }?.let { result ->
        if (onlyAcceptedOrPending) {
            result.filter { it.isAcceptedOrPending }
        } else {
            result
        }
    } ?: emptyList()
}

fun ItemGroup.getCollectionDownloads(
    myCollections: List<Collection>,
    onlyAcceptedOrPending: Boolean
): List<CollectionDownload> {
    val groupIds = myCollections.map { it.uuid }
    return collections?.filter {
        it.uuid in groupIds
    }?.let { result ->
        if (onlyAcceptedOrPending) {
            result.filter { it.status.isAcceptedOrPending }
        } else {
            result
        }
    } ?: emptyList()
}

fun ItemGroup.isAlone(login: String): Boolean {
    if (hasUserGroupsAcceptedOrPending()) return false
    if (hasCollectionsAcceptedOrPending()) return false
    if (!isUserAcceptedOrPending(login)) return false
    return users?.count { it.isAcceptedOrPending } == 1
}

fun ItemGroup.hasUserGroupsAcceptedOrPending(): Boolean =
    groups?.find { isUserGroupAcceptedOrPending(it.groupId) } != null

fun ItemGroup.hasCollectionsAcceptedOrPending(): Boolean =
    collections?.find { isCollectionAcceptedOrPending(it.uuid) } != null

fun ItemGroup.hasUserGroupsAcceptedOrPending(userGroups: List<UserGroup>): Boolean =
    userGroups.find { isUserGroupAcceptedOrPending(it.groupId) } != null

fun ItemGroup.hasCollectionsAcceptedOrPending(collections: List<Collection>): Boolean =
    collections.find { isCollectionAcceptedOrPending(it.uuid) } != null

fun ItemGroup.hasUserGroupsAccepted(userGroups: List<UserGroup>): Boolean =
    userGroups.find { isUserGroupAccepted(it.groupId) } != null

private fun ItemGroup.isUserGroupAccepted(userGroupId: String): Boolean =
    getUserGroupMember(userGroupId)?.status == Status.ACCEPTED

fun ItemGroup.isUserGroupAcceptedOrPending(userGroupId: String): Boolean =
    getUserGroupMember(userGroupId)?.isAcceptedOrPending == true

fun ItemGroup.isCollectionAcceptedOrPending(collectionId: String): Boolean =
    getCollectionDownload(collectionId)?.status?.isAcceptedOrPending == true

fun ItemGroup.isUserAccepted(username: String): Boolean =
    getUser(username)?.isAcceptedOrPending == true

fun ItemGroup.isUserAcceptedOrPending(username: String): Boolean =
    getUser(username)?.isAcceptedOrPending == true

fun ItemGroup.canLostAccess(
    login: String,
    myUserGroups: List<UserGroup>,
    myCollections: List<Collection>
): Boolean {
    if (
        getUserGroupMembers(myUserGroups, true).isNotEmpty() ||
        getCollectionMembers(myCollections, true).isNotEmpty()
    ) {
        return false
    }

    return canLostAccessBecauseOfUsers(login)
}

private fun ItemGroup.canLostAccessBecauseOfUsers(login: String): Boolean {
    return !isUserSolitaryAdmin(login)
}

fun ItemGroup.isUserSolitaryAdmin(userId: String): Boolean {
    if (hasAdminUserGroups() || hasAdminCollections()) return false
    val adminCount = countAdminUsers
    return adminCount == 1 && Permission.ADMIN == getUserPermission(userId)
}

fun ItemGroup.isUserGroupSolitaryAdmin(groupId: String): Boolean {
    groups ?: return false
    if (hasAdminCollections() || hasAdminUsers()) return false
    if (countAdminUserGroups != 1) return false
    val userGroup = getUserGroupMember(groupId) ?: return false
    return userGroup.isAdmin && userGroup.isAccepted
}

@VisibleForTesting
fun ItemGroup.hasAdminUsers(): Boolean = users?.hasAdminUsers() == true

@VisibleForTesting
fun ItemGroup.hasAdminUserGroups(): Boolean = groups?.hasAdminUserGroup() == true

private fun ItemGroup.hasAdminCollections(): Boolean = collections?.hasAdminCollection() == true

fun ItemGroup.getUserPermission(userId: String): Permission? = getUser(userId)?.permission

fun ItemGroup.isUserAdmin(userId: String) =
    getUser(userId)?.let { it.isAccepted && it.isAdmin } ?: false

fun ItemGroup.isAdmin(
    userId: String,
    myUserGroups: List<UserGroup>,
    myCollections: List<Collection>
) = isUserAdmin(userId) || isGroupAdmin(myUserGroups) || isCollectionAdmin(myCollections)

fun ItemGroup.isGroupAdmin(myUserGroups: List<UserGroup>) = groups?.any { groupMember ->
    groupMember.isAccepted && groupMember.isAdmin && myUserGroups.any {
        groupMember.groupId == it.groupId
    }
} == true

fun ItemGroup.isCollectionAdmin(myCollections: List<Collection>) = collections?.any { collection ->
    collection.isAccepted && collection.isAdmin && myCollections.any { it.uuid == collection.uuid }
} == true

private fun List<UserDownload>.hasAdminUsers(): Boolean =
    find { it.isAccepted && it.isAdmin } != null

private fun List<UserGroupMember>.hasAdminUserGroup(): Boolean =
    find { it.isAccepted && it.isAdmin } != null

private fun List<CollectionDownload>.hasAdminCollection(): Boolean =
    find { it.isAccepted && it.isAdmin } != null