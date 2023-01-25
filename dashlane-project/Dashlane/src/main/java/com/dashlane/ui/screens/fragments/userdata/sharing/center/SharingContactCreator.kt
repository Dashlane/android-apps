package com.dashlane.ui.screens.fragments.userdata.sharing.center

import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isPending
import com.dashlane.sharing.model.isUserAcceptedOrPending
import com.dashlane.sharing.model.isUserGroupAcceptedOrPending
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.datatype.ShareableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.teamspaces.manager.TeamspaceManager
import javax.inject.Inject



class SharingContactCreator @Inject constructor(
    private val dataProvider: SharingDataProviderImpl,
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val mainDataAccessor: MainDataAccessor,
) {
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()
    private val session: Session?
        get() = sessionManager.session
    private val login: String?
        get() = session?.username?.email
    private val teamspaceManager: TeamspaceManager?
        get() = teamspaceRepository[session]

    fun getItemInvites(itemGroups: List<ItemGroup>): List<SharingContact.ItemInvite> {
        val login = login ?: return emptyList()
        return itemGroups.filter { itemGroup ->
            itemGroup.getUser(login)?.isPending == true
        }.mapNotNull {
            val vaultItemId = it.items?.firstOrNull()?.itemId ?: return@mapNotNull null
            val summaryObject =
                dataProvider.getSummaryObject(vaultItemId) ?: return@mapNotNull null
            SharingContact.ItemInvite(
                itemGroup = it,
                item = summaryObject,
                login = login
            )
        }
    }

    fun getUserGroupInvites(userGroups: List<UserGroup>): List<SharingContact.UserGroupInvite> {
        val login = login ?: return emptyList()
        return userGroups.filter { userGroup ->
            userGroup.getUser(login)?.isPending == true
        }.map {
            SharingContact.UserGroupInvite(it, login)
        }
    }

    fun getUsersToDisplay(itemGroups: List<ItemGroup>): List<SharingContact.User> {
        val login = login ?: return emptyList()
        val contacts: Map<String, List<String>> =
            itemGroups
                .flatMap { itemGroup ->
                    itemGroup.users?.map { it.userId } ?: emptyList()
                }
                .toSet()
                .minus(login) 
                .associateWith { name ->
                    itemGroups.filter {
                        it.isUserAcceptedOrPending(name) && it.items != null
                    }.flatMap { itemGroup -> itemGroup.items!!.map { it.itemId } }
                }

        return contacts.map { (name, itemIds) ->
            SharingContact.User(name, itemIds)
        }.filter { isValidInCurrentSpace(it) }
    }

    fun getUserGroupsToDisplay(
        myUserGroups: List<UserGroup>,
        myItemGroups: List<ItemGroup>
    ): List<SharingContact.UserGroup> {
        val userGroupsIds = myUserGroups.map { it.groupId }
        val contacts: Map<String, List<String>> =
            myItemGroups
                .flatMap { itemGroup ->
                    itemGroup.groups?.mapNotNull {
                        if (it.groupId in userGroupsIds) it.groupId else null
                    } ?: emptyList()
                }
                .toSet()
                .associateWith { userGroupId ->
                    myItemGroups.filter {
                        it.isUserGroupAcceptedOrPending(userGroupId) && it.items != null
                    }.flatMap { itemGroup -> itemGroup.items!!.map { it.itemId } }
                }

        return myUserGroups.map {
            SharingContact.UserGroup(
                userGroup = it,
                itemCount = contacts[it.groupId]?.size ?: 0
            )
        }.filter { isValidInCurrentSpace(it) }
    }

    

    private fun isValidInCurrentSpace(
        user: SharingContact.User
    ): Boolean {
        if (isCombinedSpace()) {
            return true
        }
        return hasOneInCurrentSpace(user.itemIds)
    }

    

    private fun isValidInCurrentSpace(
        userGroup: SharingContact.UserGroup
    ): Boolean {
        if (isCombinedSpace()) {
            return true
        }
        userGroup.teamId ?: return true

        return teamspaceManager?.current?.teamId == userGroup.teamId
    }

    private fun hasOneInCurrentSpace(uids: List<String>): Boolean = getCountAvailable(uids) > 0

    private fun isCombinedSpace(): Boolean =
        teamspaceManager?.current === TeamspaceManager.COMBINED_TEAMSPACE

    private fun getCountAvailable(uids: List<String>): Int {
        if (uids.isEmpty()) {
            return 0
        }
        val filter = genericFilter {
            dataTypeFilter = ShareableDataTypeFilter
            specificUid(uids)
            forCurrentSpace()
        }
        return genericDataQuery.count(filter)
    }
}
