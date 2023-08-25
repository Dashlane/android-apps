package com.dashlane.core.sharing

import com.dashlane.exception.NotLoggedInException
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.RsaStatus
import com.dashlane.server.api.endpoints.sharinguserdevice.Status
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.sharing.model.getMaxStatus
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.getUserGroupMembers
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isPending
import com.dashlane.sharing.service.FindUsersDataProvider
import javax.inject.Inject

class SharingInvitePublicKeyUser @Inject constructor(
    private val userGroupInvitePublicKeyUser: SharingUserGroupInvitePublicKeyUser,
    private val itemGroupInvitePublicKeyUser: SharingItemGroupInvitePublicKeyUser,
    private val findUsersDataProvider: FindUsersDataProvider
) {

    @Throws(NotLoggedInException::class)
    suspend fun execute(
        session: Session,
        itemGroups: List<ItemGroup>,
        myUserGroupsAcceptedOrPending: List<UserGroup>
    ): Pair<List<ItemGroup>, List<UserGroup>> {
        val login = session.userId

        
        val itemGroupsAccepted =
            itemGroups.filterItemGroupWhereAccepted(login, myUserGroupsAcceptedOrPending)
        
        val userGroupsAccepted = myUserGroupsAcceptedOrPending.filterUserGroupWhereAccepted(login)

        val contactEmails = mutableSetOf<String>()
        val usersToRequest = mutableMapOf<String, List<UserDownload>>()

        itemGroupsAccepted.forEach { updateUsersToRequest(it, contactEmails, usersToRequest) }
        userGroupsAccepted.forEach { updateUsersToRequest(it, contactEmails, usersToRequest) }
        if (contactEmails.isEmpty()) return Pair(listOf(), listOf())
        val users = executeGetUsers(session, contactEmails)
        if (users.isEmpty()) return Pair(listOf(), listOf())
        return executeSendInvitation(
            session,
            users,
            usersToRequest,
            itemGroupsAccepted,
            userGroupsAccepted
        )
    }

    private suspend fun executeGetUsers(
        session: Session,
        contactEmails: Set<String>
    ): List<GetUsersPublicKeyService.Data.Data> {
        return findUsersDataProvider.findUsers(session, contactEmails.toList())
    }

    private suspend fun executeSendInvitation(
        session: Session,
        users: List<GetUsersPublicKeyService.Data.Data>,
        usersToRequest: Map<String, List<UserDownload>>,
        itemGroupsAccepted: List<ItemGroup>,
        userGroupsAccepted: List<UserGroup>
    ): Pair<List<ItemGroup>, List<UserGroup>> {
        
        val newItemsGroups = itemGroupInvitePublicKeyUser
            .execute(session, itemGroupsAccepted, userGroupsAccepted, usersToRequest, users)

        
        val newUserGroups = userGroupInvitePublicKeyUser
            .execute(session, userGroupsAccepted, usersToRequest, users)

        return newItemsGroups to newUserGroups
    }

    private fun updateUsersToRequest(
        group: ItemGroup,
        contactEmails: MutableSet<String>,
        usersToRequest: MutableMap<String, List<UserDownload>>
    ) {
        val userDownloads = group.users
        if (userDownloads.isNullOrEmpty()) {
            return
        }
        val users = userDownloads.filter { it.isPending && RsaStatus.PUBLICKEY == it.rsaStatus }
        if (users.isNotEmpty()) {
            users.forEach { contactEmails.add(it.alias) }
            usersToRequest[group.groupId] = users
        }
    }

    private fun updateUsersToRequest(
        group: UserGroup,
        contactEmails: MutableSet<String>,
        usersToRequest: MutableMap<String, List<UserDownload>>
    ) {
        val userDownloads = group.users
        if (userDownloads.isEmpty()) {
            return
        }
        val users = userDownloads.filter { it.isPending && RsaStatus.PUBLICKEY == it.rsaStatus }
        if (users.isNotEmpty()) {
            users.forEach { contactEmails.add(it.alias) }
            usersToRequest[group.groupId] = users
        }
    }

    private fun List<ItemGroup>.filterItemGroupWhereAccepted(
        login: String,
        myUserGroupsAcceptedOrPending: List<UserGroup>
    ): List<ItemGroup> {
        return filter { isAcceptedAsUserOrGroup(it, login, myUserGroupsAcceptedOrPending) }
    }

    private fun List<UserGroup>.filterUserGroupWhereAccepted(login: String): List<UserGroup> {
        return filter { it.getUser(login)?.isAccepted == true }
    }

    private fun isAcceptedAsUserOrGroup(
        itemGroup: ItemGroup,
        login: String,
        myUserGroupsAcceptedOrPending: List<UserGroup>
    ): Boolean {
        val userDownload = itemGroup.getUser(login) ?: return false
        val myUserGroupsMember = itemGroup.getUserGroupMembers(myUserGroupsAcceptedOrPending)
        return Status.ACCEPTED == getMaxStatus(userDownload, myUserGroupsMember)
    }
}
