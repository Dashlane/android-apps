package com.dashlane.core.sharing

import com.dashlane.exception.NotLoggedInException
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.GetUsersPublicKeyService
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.RsaStatus
import com.dashlane.server.api.endpoints.sharinguserdevice.Status
import com.dashlane.server.api.endpoints.sharinguserdevice.UserCollectionDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserDownload
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.sharing.model.getMaxStatus
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.getUserGroupMembers
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.model.isPending
import com.dashlane.sharing.service.FindUsersDataProvider
import com.dashlane.sharing.util.intersectUserGroupCollectionDownload
import javax.inject.Inject

class SharingInvitePublicKeyUser @Inject constructor(
    private val userGroupInvitePublicKeyUser: SharingUserGroupInvitePublicKeyUser,
    private val itemGroupInvitePublicKeyUser: SharingItemGroupInvitePublicKeyUser,
    private val collectionInvitePublicKeyUser: SharingCollectionInvitePublicKeyUser,
    private val findUsersDataProvider: FindUsersDataProvider
) {

    @Throws(NotLoggedInException::class)
    suspend fun execute(
        session: Session,
        itemGroups: List<ItemGroup>,
        myUserGroupsAcceptedOrPending: List<UserGroup>,
        myCollectionsAcceptedOrPending: List<Collection>
    ): Triple<List<ItemGroup>, List<UserGroup>, List<Collection>> {
        val login = session.userId

        
        val userGroupsAccepted = myUserGroupsAcceptedOrPending.filterUserGroupWhereAccepted(login)
        
        val myCollectionsAccepted =
            myCollectionsAcceptedOrPending.filterCollectionWhereAccepted(login, userGroupsAccepted)
        
        val itemGroupsAccepted =
            itemGroups.filterItemGroupWhereAccepted(
                login,
                myUserGroupsAcceptedOrPending,
                myCollectionsAccepted
            )

        val contactEmails = mutableSetOf<String>()
        val usersToRequest = mutableMapOf<String, List<UserDownload>>()
        val collectionUsersToRequest = mutableMapOf<String, List<UserCollectionDownload>>()

        itemGroupsAccepted.forEach {
            if (it.isAdmin(login, userGroupsAccepted, myCollectionsAccepted)) {
                updateUsersToRequest(it, contactEmails, usersToRequest)
            }
        }
        userGroupsAccepted.forEach {
            if (it.getUser(login)?.isAdmin == true) {
                updateUsersToRequest(it, contactEmails, usersToRequest)
            }
        }
        myCollectionsAccepted.forEach {
            if (it.isAdmin(login, userGroupsAccepted)) {
                updateCollectionUsersToRequest(it, contactEmails, collectionUsersToRequest)
            }
        }

        if (contactEmails.isEmpty()) return Triple(listOf(), listOf(), listOf())
        val users = executeGetUsers(session, contactEmails)
        if (users.isEmpty()) return Triple(listOf(), listOf(), listOf())
        return executeSendInvitation(
            session,
            users,
            usersToRequest,
            collectionUsersToRequest,
            itemGroupsAccepted,
            userGroupsAccepted,
            myCollectionsAccepted
        )
    }

    private fun updateCollectionUsersToRequest(
        collection: Collection,
        contactEmails: MutableSet<String>,
        collectionUsersToRequest: MutableMap<String, List<UserCollectionDownload>>
    ) {
        val users = collection.users?.filter { user ->
            user.isPending && RsaStatus.PUBLICKEY == user.rsaStatus
        } ?: return
        users.forEach { contactEmails.add(it.login) }
        collectionUsersToRequest[collection.uuid] = users
    }

    private suspend fun executeGetUsers(
        session: Session,
        contactEmails: Set<String>
    ): List<GetUsersPublicKeyService.Data.Data> {
        return findUsersDataProvider.findUsers(session, contactEmails.distinct())
    }

    private suspend fun executeSendInvitation(
        session: Session,
        users: List<GetUsersPublicKeyService.Data.Data>,
        usersToRequest: Map<String, List<UserDownload>>,
        collectionUsersToRequest: Map<String, List<UserCollectionDownload>>,
        itemGroupsAccepted: List<ItemGroup>,
        userGroupsAccepted: List<UserGroup>,
        collectionsAccepted: List<Collection>
    ): Triple<List<ItemGroup>, List<UserGroup>, List<Collection>> {
        
        val newItemsGroups = itemGroupInvitePublicKeyUser
            .execute(session, itemGroupsAccepted, userGroupsAccepted, usersToRequest, users)

        
        val newUserGroups = userGroupInvitePublicKeyUser
            .execute(session, userGroupsAccepted, usersToRequest, users)

        
        val newCollections = collectionInvitePublicKeyUser.execute(
            session,
            collectionsAccepted,
            userGroupsAccepted,
            collectionUsersToRequest,
            users
        )

        return Triple(newItemsGroups, newUserGroups, newCollections)
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
        myUserGroupsAcceptedOrPending: List<UserGroup>,
        myCollectionsAccepted: List<Collection>
    ): List<ItemGroup> {
        return filter {
            isAcceptedAsUserOrGroupOrCollection(
                it,
                login,
                myUserGroupsAcceptedOrPending,
                myCollectionsAccepted
            )
        }
    }

    private fun List<UserGroup>.filterUserGroupWhereAccepted(login: String): List<UserGroup> {
        return filter { it.getUser(login)?.isAccepted == true }
    }

    private fun List<Collection>.filterCollectionWhereAccepted(
        login: String,
        acceptedUserGroups: List<UserGroup>
    ): List<Collection> {
        val myCollectionUserGroups = mapNotNull { it.userGroups }.flatten()
            .filter { it.isAccepted }
            .intersectUserGroupCollectionDownload(acceptedUserGroups)
        return filter {
            it.userGroups?.any { group -> myCollectionUserGroups.contains(group) } == true ||
                it.users?.any { user -> user.login == login && user.isAccepted } == true
        }
    }

    private fun isAcceptedAsUserOrGroupOrCollection(
        itemGroup: ItemGroup,
        login: String,
        myUserGroupsAcceptedOrPending: List<UserGroup>,
        myCollectionsAccepted: List<Collection>
    ): Boolean {
        val userDownload = itemGroup.getUser(login) ?: return false
        val myUserGroupsMember = itemGroup.getUserGroupMembers(myUserGroupsAcceptedOrPending)
        val myCollectionsMember = itemGroup.collections?.filter {
            myCollectionsAccepted.any { collection -> collection.uuid == it.uuid }
        } ?: emptyList()
        return Status.ACCEPTED == getMaxStatus(
            userDownload,
            myUserGroupsMember,
            myCollectionsMember
        )
    }
}
