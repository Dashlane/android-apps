package com.dashlane.sharing.model

import androidx.annotation.VisibleForTesting
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

fun ItemGroup.getUserGroupMembers(userGroups: List<UserGroup>): List<UserGroupMember> =
    getUserGroupMembers(userGroups, false)

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

fun ItemGroup.isAlone(login: String): Boolean {
    if (hasUserGroupsAcceptedOrPending()) {
        return false
    }
    if (!isUserAcceptedOrPending(login)) return false
    return users?.count { it.isAcceptedOrPending } == 1
}

fun ItemGroup.hasUserGroupsAcceptedOrPending(): Boolean =
    groups?.find { isUserGroupAcceptedOrPending(it.groupId) } != null

fun ItemGroup.hasUserGroupsAcceptedOrPending(userGroups: List<UserGroup>): Boolean =
    userGroups.find { isUserGroupAcceptedOrPending(it.groupId) } != null

fun ItemGroup.hasUserGroupsAccepted(userGroups: List<UserGroup>): Boolean =
    userGroups.find { isUserGroupAccepted(it.groupId) } != null

private fun ItemGroup.isUserGroupAccepted(userGroupId: String): Boolean =
    getUserGroupMember(userGroupId)?.status == Status.ACCEPTED

fun ItemGroup.isUserGroupAcceptedOrPending(userGroupId: String): Boolean =
    getUserGroupMember(userGroupId)?.isAcceptedOrPending == true

fun ItemGroup.isUserAccepted(username: String): Boolean =
    getUser(username)?.isAcceptedOrPending == true

fun ItemGroup.isUserAcceptedOrPending(username: String): Boolean =
    getUser(username)?.isAcceptedOrPending == true

fun ItemGroup.canLostAccess(login: String, userGroups: List<UserGroup>): Boolean {
    val userGroupMembers = getUserGroupMembers(userGroups, true)
    if (userGroupMembers.isNotEmpty()) {
        return false 
    }
    return canLostAccessBecauseOfUsers(login)
}

private fun ItemGroup.canLostAccessBecauseOfUsers(login: String): Boolean {
    val me = getUser(login) ?: return true
    if (!me.isAdmin) return true
    return countAdminUsers > 1
}

fun ItemGroup.isUserSolitaryAdmin(userId: String): Boolean {
    if (hasAdminUserGroups()) return false
    val adminCount = countAdminUsers
    return adminCount == 1 && Permission.ADMIN == getUserPermission(userId)
}

fun ItemGroup.isUserGroupSolitaryAdmin(groupId: String): Boolean {
    groups ?: return false
    if (hasAdminUsers() || !hasAdminUserGroups()) {
        return false
    }
    if (countAdminUserGroups != 1) return false
    val userGroup = getUserGroupMember(groupId) ?: return false
    return userGroup.isAdmin && userGroup.isAccepted
}

@VisibleForTesting
fun ItemGroup.hasAdminUsers(): Boolean = users?.hasAdminUsers() == true

@VisibleForTesting
fun ItemGroup.hasAdminUserGroups(): Boolean = groups?.hasAdminUserGroup() == true

fun ItemGroup.getUserStatus(userId: String): String? = getUser(userId)?.status?.key

fun ItemGroup.getUserPermission(userId: String): Permission? = getUser(userId)?.permission

private fun List<UserDownload>.hasAdminUsers(): Boolean =
    find { it.isAccepted && it.isAdmin } != null

private fun List<UserGroupMember>.hasAdminUserGroup(): Boolean =
    find { it.isAccepted && it.isAdmin } != null
