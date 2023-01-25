package com.dashlane.notification.badge

import com.dashlane.loaders.datalists.SharingUserDataUtils
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isPending
import com.dashlane.storage.DataStorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SharingInvitationRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val dataStorageProvider: DataStorageProvider
) : SharingInvitationRepository {

    private val predicateItemGroup: (ItemGroup) -> Boolean = { itemGroup ->
        sessionManager.session?.userId?.let { username ->
            itemGroup.getUser(username)?.isPending == true
        } ?: false
    }

    private val predicateUserGroup: (UserGroup) -> Boolean = { userGroup ->
        sessionManager.session?.userId?.let { username ->
            SharingUserDataUtils.isMemberOfUserGroupTeam(userGroup) &&
                    userGroup.getUser(username)?.isPending == true
        } ?: false
    }

    override suspend fun hasInvitations(): Boolean {
        val anyItemInvitation = loadItemGroups().any(predicateItemGroup)
        if (anyItemInvitation) return true

        return loadUserGroups().any(predicateUserGroup)
    }

    suspend fun loadAllInvitations() = coroutineScope {
        val itemGroupList = async {
            loadItemGroups().filter(predicateItemGroup)
        }

        val userGroupList = async {
            loadUserGroups().filter(predicateUserGroup)
        }

        itemGroupList.await() to userGroupList.await()
    }

    private suspend fun loadItemGroups(): List<ItemGroup> = withContext(Dispatchers.Default) {
        dataStorageProvider.sharingDao.loadAllItemGroup()
    }

    private suspend fun loadUserGroups(): List<UserGroup> = withContext(Dispatchers.Default) {
        dataStorageProvider.sharingDao.loadAllUserGroup()
    }
}