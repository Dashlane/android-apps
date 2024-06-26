package com.dashlane.core.sharing

import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.cryptography.decodeBase64ToByteArrayOrNull
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.Collection
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemContent
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.sharing.SharingSyncCommunicator
import com.dashlane.sharing.internal.model.ItemToUpdate
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.storage.userdata.dao.SharingDataType
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.sync.sharing.SharingSync
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class SharingSyncImpl @Inject constructor(
    private val xmlConverter: DataIdentifierSharingXmlConverter,
    private val sharingCommunicator: SharingSyncCommunicator,
    private val sharingCryptographyHelper: SharingCryptographyHelper,
    private val sharingDao: SharingDao,
    private val sharingItemUpdater: SharingItemUpdater,
    private val sharingGetProvider: SharingGetProvider
) : SharingSync {

    override suspend fun syncSharing(
        session: Authorization.User,
        sharingSummary: SyncDownloadService.Data.SharingSummary
    ) {
        runCatching { doUpdate(session, sharingSummary) }
    }

    private suspend fun doUpdate(
        session: Authorization.User,
        summary: SyncDownloadService.Data.SharingSummary
    ) {
        val (itemUidToUpdate, itemUidToDelete) =
            getUidOutdated(SharingDataType.ITEM, summary.items.associate { it.id to it.timestamp })

        val (itemGroupUidToUpdate, itemGroupUidToDelete) =
            getUidOutdated(
                SharingDataType.ITEM_GROUP,
                summary.itemGroups.associate { it.id to it.revision }
            )

        val (userGroupUidToUpdate, userGroupUidToDelete) =
            getUidOutdated(
                SharingDataType.USER_GROUP,
                summary.userGroups.associate { it.id to it.revision }
            )

        val (collectionUidToUpdate, collectionUidToDelete) =
            getUidOutdated(
                SharingDataType.COLLECTION,
                summary.collections.associate { it.id to it.revision }
            )

        requestUpdate(
            session,
            SharingSync.IdCollection(itemUidToUpdate, itemGroupUidToUpdate, userGroupUidToUpdate, collectionUidToUpdate),
            SharingSync.IdCollection(itemUidToDelete, itemGroupUidToDelete, userGroupUidToDelete, collectionUidToDelete)
        )

        sendItemsLocalUpdates(session, summary)
    }

    private suspend fun requestUpdate(
        session: Authorization.User,
        idsToRequest: SharingSync.IdCollection,
        idsToDelete: SharingSync.IdCollection
    ) {
        val result = sharingGetProvider.requestUpdate(
            session,
            idsToRequest
        )
        if (!result.isEmptyResult) {
            updateItemLocally(
                itemGroups = result.itemGroups,
                itemContents = result.itemContents,
                userGroupDownloads = result.userGroups,
                collections = result.collections,
                idsToDelete = idsToDelete
            )
        } else if (!idsToDelete.isEmpty) {
            updateItemLocally(
                idsToDelete = idsToDelete
            )
        }
    }

    private suspend fun updateItemLocally(
        itemGroups: List<ItemGroup> = emptyList(),
        itemContents: List<ItemContent> = emptyList(),
        userGroupDownloads: List<UserGroup> = emptyList(),
        collections: List<Collection> = emptyList(),
        idsToDelete: SharingSync.IdCollection
    ) {
        sharingItemUpdater.update(
            SharingItemUpdaterRequest(
                itemGroupUpdates = itemGroups,
                itemContentUpdates = itemContents,
                userGroupUpdates = userGroupDownloads,
                collectionUpdates = collections,
                itemsDeletionIds = idsToDelete.items,
                itemGroupsDeletionIds = idsToDelete.itemGroups,
                userGroupDeletionIds = idsToDelete.userGroups,
                collectionsDeletionIds = idsToDelete.collections
            )
        )
    }

    private suspend fun sendItemsLocalUpdates(
        session: Authorization.User,
        summary: SyncDownloadService.Data.SharingSummary
    ) {
        val itemToUpdateList = sharingDao.getDirtyForSharing().mapNotNull { dataIdentifier ->
            getItemToUpdateFrom(dataIdentifier, summary)
        }
        if (itemToUpdateList.isNotEmpty()) {
            val itemContentSent = sharingCommunicator.updateItems(session, itemToUpdateList)
            for (itemContent in itemContentSent) {
                val itemId = itemContent.itemId
                sharingDao.updateItemTimestamp(itemId, itemContent.timestamp.toLong())
            }
            sharingDao.markAsShared(itemContentSent.map { it.itemId })
        }
    }

    private suspend fun getItemToUpdateFrom(
        dataIdentifier: DataIdentifierExtraDataWrapper<out SyncObject>,
        summary: SyncDownloadService.Data.SharingSummary
    ): ItemToUpdate? {
        val uid = dataIdentifier.vaultItem.uid
        val syncTimestamp = summary.items.firstOrNull { it.id == uid }?.timestamp
        val (itemKeyBase64, remoteTimestamp) = sharingDao.getItemKeyTimestamp(uid) ?: run {
            return null
        }
        
        
        
        val timestamp = syncTimestamp ?: remoteTimestamp
        val content = xmlConverter.toXml(dataIdentifier)
        val itemKey = itemKeyBase64.decodeBase64ToByteArrayOrNull()
        if (itemKey == null) {
            return null
        }
        val contentEncrypted =
            sharingCryptographyHelper.encryptItemContent(content, itemKey) ?: return null
        return ItemToUpdate(uid, contentEncrypted, timestamp)
    }

    private suspend fun getUidOutdated(
        dataType: SharingDataType,
        remoteRevisionById: Map<String, Long>
    ): Pair<List<String>, List<String>> {
        val localRevisionById = runCatching { sharingDao.getItemsSummary(dataType) }
            .getOrElse { return emptyList<String>() to emptyList() }
            .toMap()

        val deletedIds = localRevisionById.keys - remoteRevisionById.keys
        val updatedIds =
            remoteRevisionById.filter { (id, revision) -> localRevisionById[id] != revision }.keys

        return updatedIds.toList() to deletedIds.toList()
    }
}
