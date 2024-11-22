package com.dashlane.ui.screens.fragments.userdata.sharing.users

import com.dashlane.core.sharing.SharingDao
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.sharing.model.getCollectionDownload
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAdmin
import com.dashlane.sharing.model.isPending
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.ui.screens.fragments.userdata.sharing.SharingModels
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.ui.screens.fragments.userdata.sharing.getSharingStatusResource
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SharingUsersDataProvider @Inject constructor(
    private val sharingDao: SharingDao,
    private val genericDataQuery: GenericDataQuery,
    private val sharingDataProvider: SharingDataProvider,
    @IoCoroutineDispatcher
    private val ioCoroutineDispatcher: CoroutineDispatcher
) {
    suspend fun getContactsForItem(itemId: String): List<SharingModels> {
        return withContext(ioCoroutineDispatcher) {
            val itemGroup = sharingDao.loadItemGroupForItem(itemId)
                ?: return@withContext emptyList<SharingModels>()
            val collections = sharingDataProvider.getCollections(itemId)
            val summaryObject = genericDataQuery.queryFirst(
                genericFilter {
                    specificUid(itemId)
                }
            ) ?: return@withContext emptyList<SharingModels>()

            val collectionGroups = loadCollectionsGroups(collections, itemGroup, summaryObject)
            val groups = loadGroups(itemGroup, summaryObject)
            val collectionUsers = loadCollectionsUsers(collections, itemGroup, summaryObject)
            val users = loadUsers(itemGroup, summaryObject)
            (collectionGroups + groups).sortedByDescending { it.isMemberAdmin }.distinctBy { it.groupId } +
                (collectionUsers + users).sortedByDescending { it.isMemberAdmin }.distinctBy { it.userId }
        }
    }

    private fun loadUsers(
        itemGroup: ItemGroup,
        summaryObject: SummaryObject
    ) = itemGroup.users?.map {
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
    ) = collections.map {
        it.users?.map { userCollection ->
            SharingModels.ItemUser(
                userId = userCollection.login,
                isAccepted = userCollection.isAccepted,
                isPending = userCollection.isPending,
                isMemberAdmin = itemGroup.getCollectionDownload(it.uuid)?.isAdmin == true,
                sharingStatusResource = itemGroup.getCollectionDownload(it.uuid)
                    ?.getSharingStatusResource() ?: 0,
                itemGroup = itemGroup,
                item = summaryObject,
                isItemInCollection = true
            )
        } ?: emptyList()
    }.flatten()

    private fun loadGroups(
        itemGroup: ItemGroup,
        summaryObject: SummaryObject
    ) = itemGroup.groups?.map {
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
    ) = collections.map {
        it.userGroups?.map { userCollectionGroup ->
            SharingModels.ItemUserGroup(
                groupId = userCollectionGroup.uuid,
                name = userCollectionGroup.name,
                isAccepted = userCollectionGroup.isAccepted,
                isPending = userCollectionGroup.isPending,
                isMemberAdmin = itemGroup.getCollectionDownload(it.uuid)?.isAdmin == true,
                sharingStatusResource = itemGroup.getCollectionDownload(it.uuid)
                    ?.getSharingStatusResource() ?: 0,
                itemGroup = itemGroup,
                item = summaryObject,
                isItemInCollection = true
            )
        } ?: emptyList()
    }.flatten()
}
