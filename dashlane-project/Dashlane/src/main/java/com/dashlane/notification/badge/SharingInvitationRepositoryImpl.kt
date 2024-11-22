package com.dashlane.notification.badge

import com.dashlane.core.sharing.SharingDao
import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.loaders.datalists.SharingUserDataUtils
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isPending
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SharingInvitationRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val sharingDao: SharingDao,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    private val sharingUserDataUtils: SharingUserDataUtils,
    private val xmlConverter: DataIdentifierSharingXmlConverter,
) : SharingInvitationRepository {

    private val predicateItemGroup: (ItemGroup) -> Boolean = { itemGroup ->
        sessionManager.session?.userId?.let { username ->
            itemGroup.getUser(username)?.isPending == true
        } ?: false
    }

    private val predicateUserGroup: (UserGroup) -> Boolean = { userGroup ->
        sessionManager.session?.userId?.let { username ->
            sharingUserDataUtils.isMemberOfUserGroupTeam(userGroup) &&
                userGroup.getUser(username)?.isPending == true
        } ?: false
    }

    private val predicateCollection: (Collection) -> Boolean = { collection ->
        sessionManager.session?.userId?.let { username ->
            collection.getUser(username)?.isPending == true
        } ?: false
    }

    override suspend fun hasInvitations() = loadItemGroups().any(predicateItemGroup) ||
        loadUserGroups().any(predicateUserGroup) ||
        loadCollections().any(predicateCollection)

    suspend fun loadAllInvitations() = coroutineScope {
        val itemGroupList = async {
            loadItemGroups()
                .filter(predicateItemGroup)
                .map { itemGroup ->
                    itemGroup.copy(
                        items = itemGroup.items?.filter { item ->
                            val xml = sharingDao.loadItemContentExtraData(item.itemId)
                            val syncObjectType = xmlConverter.fromXml(item.itemId, xml)?.vaultItem?.syncObjectType
                            syncObjectType != SyncObjectType.SECRET
                        }
                    )
                }
                .filter { it.items?.isNotEmpty() == true }
        }

        val userGroupList = async {
            loadUserGroups().filter(predicateUserGroup)
        }

        val collectionList = async {
            loadCollections().filter(predicateCollection)
        }
        SharingInvitations(
            itemGroupInvitations = itemGroupList.await(),
            userGroupInvitations = userGroupList.await(),
            collectionInvitations = collectionList.await()
        )
    }

    private suspend fun loadItemGroups() = withContext(defaultDispatcher) {
        sharingDao.loadAllItemGroup()
    }

    private suspend fun loadUserGroups() = withContext(defaultDispatcher) {
        sharingDao.loadAllUserGroup()
    }

    private suspend fun loadCollections() = withContext(defaultDispatcher) {
        sharingDao.loadAllCollection()
    }

    data class SharingInvitations(
        val itemGroupInvitations: List<ItemGroup>,
        val userGroupInvitations: List<UserGroup>,
        val collectionInvitations: List<Collection>
    )
}