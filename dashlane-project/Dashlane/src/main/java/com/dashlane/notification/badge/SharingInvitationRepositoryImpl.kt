package com.dashlane.notification.badge

import com.dashlane.loaders.datalists.SharingUserDataUtils
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.session.SessionManager
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isPending
import com.dashlane.storage.DataStorageProvider
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class SharingInvitationRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val dataStorageProvider: DataStorageProvider,
    @DefaultCoroutineDispatcher
    private val defaultDispatcher: CoroutineDispatcher,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val sharingUserDataUtils: SharingUserDataUtils,
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
            loadItemGroups().filter(predicateItemGroup)
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
        dataStorageProvider.sharingDao.loadAllItemGroup()
    }

    private suspend fun loadUserGroups() = withContext(defaultDispatcher) {
        dataStorageProvider.sharingDao.loadAllUserGroup()
    }

    private suspend fun loadCollections() = withContext(defaultDispatcher) {
        if (!userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION_MILESTONE_2)) {
            return@withContext emptyList()
        }
        dataStorageProvider.sharingDao.loadAllCollection()
    }

    data class SharingInvitations(
        val itemGroupInvitations: List<ItemGroup>,
        val userGroupInvitations: List<UserGroup>,
        val collectionInvitations: List<Collection>
    )
}