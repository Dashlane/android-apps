package com.dashlane.ui.screens.fragments.userdata.sharing.users

import com.dashlane.core.sharing.SharingDao
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.model.isPending
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.getSharingStatusResource
import com.dashlane.userfeatures.FeatureFlip
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SharingUsersDataProvider @Inject constructor(
    private val sharingDao: SharingDao,
    private val genericDataQuery: GenericDataQuery,
    private val sharingDataProvider: SharingDataProvider,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher,
    private val userFeaturesChecker: UserFeaturesChecker
) {
    private val showSharedCollections: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SHARING_COLLECTION)

    suspend fun getContactsForItem(itemId: String): List<SharingModels> {
        return withContext(ioCoroutineDispatcher) {
            val itemGroup = sharingDao.loadItemGroupForItem(itemId)
                ?: return@withContext emptyList<SharingModels>()
            val collections = sharingDataProvider.getCollections(itemId, needsAdminRights = false)
            val summaryObject = genericDataQuery.queryFirst(
                genericFilter {
                    specificUid(itemId)
                }
            ) ?: return@withContext emptyList<SharingModels>()

            val collectionGroups = loadCollectionsGroups(collections, itemGroup, summaryObject)
            val groups = loadGroups(itemGroup, collectionGroups, summaryObject)
            val collectionUsers = loadCollectionsUsers(collections, itemGroup, summaryObject)
            val users = loadUsers(itemGroup, collectionUsers, summaryObject)
            (collectionGroups + groups).distinctBy { it.groupId } +
                (collectionUsers + users).distinctBy { it.userId }
        }
    }

    private fun loadUsers(
        itemGroup: ItemGroup,
        collectionUsers: List<SharingModels.ItemUser>,
        summaryObject: SummaryObject
    ) = itemGroup.users?.mapNotNull {
        if (collectionUsers.any { user -> user.userId == it.userId }) {
            return@mapNotNull null
        }
        SharingModels.ItemUser(
            userId = it.userId,
            isAccepted = it.isAccepted,
            isPending = it.isPending,
            isMemberAdmin = it.isAdmin,
            sharingStatusResource = it.getSharingStatusResource(),
            itemGroup = itemGroup,
            item = summaryObject,
            isItemInCollection = false
        )
    } ?: emptyList()

    private fun loadCollectionsUsers(
        collections: List<Collection>,
        itemGroup: ItemGroup,
        summaryObject: SummaryObject
    ) = if (showSharedCollections) {
        collections.map {
            it.users?.map { userCollection ->
                SharingModels.ItemUser(
                    userId = userCollection.login,
                    isAccepted = userCollection.isAccepted,
                    isPending = userCollection.isPending,
                    isMemberAdmin = userCollection.isAdmin,
                    sharingStatusResource = userCollection.getSharingStatusResource(),
                    itemGroup = itemGroup,
                    item = summaryObject,
                    isItemInCollection = true
                )
            } ?: emptyList()
        }.flatten()
    } else {
        emptyList()
    }

    private fun loadGroups(
        itemGroup: ItemGroup,
        collectionGroups: List<SharingModels.ItemUserGroup>,
        summaryObject: SummaryObject
    ) = itemGroup.groups?.mapNotNull {
        if (collectionGroups.any { group -> group.groupId == it.groupId }) {
            return@mapNotNull null
        }
        SharingModels.ItemUserGroup(
            groupId = it.groupId,
            name = it.name,
            isAccepted = it.isAccepted,
            isPending = it.isPending,
            isMemberAdmin = it.isAdmin,
            sharingStatusResource = it.getSharingStatusResource(),
            itemGroup = itemGroup,
            item = summaryObject,
            isItemInCollection = false
        )
    } ?: emptyList()

    private fun loadCollectionsGroups(
        collections: List<Collection>,
        itemGroup: ItemGroup,
        summaryObject: SummaryObject
    ) = if (showSharedCollections) {
        collections.map {
            it.userGroups?.map { userCollectionGroup ->
                SharingModels.ItemUserGroup(
                    groupId = userCollectionGroup.uuid,
                    name = userCollectionGroup.name,
                    isAccepted = userCollectionGroup.isAccepted,
                    isPending = userCollectionGroup.isPending,
                    isMemberAdmin = userCollectionGroup.isAdmin,
                    sharingStatusResource = userCollectionGroup.getSharingStatusResource(),
                    itemGroup = itemGroup,
                    item = summaryObject,
                    isItemInCollection = true
                )
            } ?: emptyList()
        }.flatten()
    } else {
        emptyList()
    }
}
