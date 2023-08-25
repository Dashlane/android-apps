package com.dashlane.core.sharing

import com.dashlane.core.xmlconverter.DataIdentifierSharingXmlConverter
import com.dashlane.cryptography.decodeBase64ToByteArrayOrNull
import com.dashlane.server.api.Authorization
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemContent
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.sharing.SharingSyncCommunicator
import com.dashlane.sharing.internal.model.ItemToUpdate
import com.dashlane.sharing.util.SharingCryptographyHelper
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.dao.SharingDataType
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.sync.sharing.SharingSync
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class SharingSyncImpl @Inject constructor(
    private val xmlConverter: DataIdentifierSharingXmlConverter,
    private val sharingCommunicator: SharingSyncCommunicator,
    private val sharingCryptographyHelper: SharingCryptographyHelper,
    private val dataStorageProvider: DataStorageProvider,
    private val sharingItemUpdater: SharingItemUpdater,
    private val sharingGetProvider: SharingGetProvider
) : SharingSync {

    private val sharingSyncDao: SharingDao
        get() = dataStorageProvider.sharingDao

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

        requestUpdate(
            session,
            SharingSync.IdCollection(itemUidToUpdate, itemGroupUidToUpdate, userGroupUidToUpdate),
            SharingSync.IdCollection(itemUidToDelete, itemGroupUidToDelete, userGroupUidToDelete)
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
        idsToDelete: SharingSync.IdCollection
    ) {
        sharingItemUpdater.update(
            SharingItemUpdaterRequest(
                itemGroupUpdates = itemGroups,
                itemContentUpdates = itemContents,
                userGroupUpdates = userGroupDownloads,
                itemsDeletionIds = idsToDelete.items,
                itemGroupsDeletionIds = idsToDelete.itemGroups,
                userGroupDeletionIds = idsToDelete.userGroups
            )
        )
    }

    private suspend fun sendItemsLocalUpdates(
        session: Authorization.User,
        summary: SyncDownloadService.Data.SharingSummary
    ) {
        val itemToUpdateList = sharingSyncDao.getDirtyForSharing().mapNotNull { dataIdentifier ->
            getItemToUpdateFrom(dataIdentifier, summary)
        }
        if (itemToUpdateList.isNotEmpty()) {
            val itemContentSent = sharingCommunicator.updateItems(session, itemToUpdateList)
            for (itemContent in itemContentSent) {
                val itemId = itemContent.itemId
                sharingSyncDao.updateItemTimestamp(itemId, itemContent.timestamp.toLong())
            }
            sharingSyncDao.markAsShared(itemContentSent.map { it.itemId })
        }
    }

    private suspend fun getItemToUpdateFrom(
        dataIdentifier: DataIdentifierExtraDataWrapper<out SyncObject>,
        summary: SyncDownloadService.Data.SharingSummary
    ): ItemToUpdate? {
        val uid = dataIdentifier.vaultItem.uid
        val syncTimestamp = summary.items.firstOrNull { it.id == uid }?.timestamp
        val (itemKeyBase64, remoteTimestamp) = sharingSyncDao.getItemKeyTimestamp(uid) ?: run {
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
        val localRevisionById = runCatching { sharingSyncDao.getItemsSummary(dataType) }
            .getOrElse { return emptyList<String>() to emptyList() }
            .toMap()

        val deletedIds = localRevisionById.keys - remoteRevisionById.keys
        val updatedIds =
            remoteRevisionById.filter { (id, revision) -> localRevisionById[id] != revision }.keys

        return updatedIds.toList() to deletedIds.toList()
    }
}
