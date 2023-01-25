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
import com.dashlane.util.logD
import com.dashlane.util.logI
import com.dashlane.util.logV
import com.dashlane.util.logW
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject



class SharingSyncImpl @Inject constructor(
    private val xmlConverter: DataIdentifierSharingXmlConverter,
    private val sharingCommunicator: SharingSyncCommunicator,
    private val sharingCryptographyHelper: SharingCryptographyHelper,
    private val dataStorageProvider: DataStorageProvider,
    private val sharingItemUpdater: SharingItemUpdater
) : SharingSync {

    private val sharingSyncDao: SharingDao
        get() = dataStorageProvider.sharingDao

    

    override suspend fun syncSharing(session: Authorization.User, sharingSummary: SyncDownloadService.Data.SharingSummary) {
        logI { "Starting sharing sync" }
        runCatching { doUpdate(session, sharingSummary) }
            .onFailure { logW(throwable = it) { "Sharing sync failed" } }
        logI { "Sharing sync completed" }
    }

    private suspend fun doUpdate(
        session: Authorization.User,
        summary: SyncDownloadService.Data.SharingSummary
    ) {
        val (itemUidToUpdate, itemUidToDelete) =
            getUidOutdated(SharingDataType.ITEM, summary.items.associate { it.id to it.timestamp })
        logD { "Pending item updates: $itemUidToUpdate; deletions: $itemUidToDelete" }

        val (itemGroupUidToUpdate, itemGroupUidToDelete) =
            getUidOutdated(
                SharingDataType.ITEM_GROUP,
                summary.itemGroups.associate { it.id to it.revision })
        logD { "Pending item group updates: $itemGroupUidToUpdate; deletions: $itemGroupUidToDelete" }

        val (userGroupUidToUpdate, userGroupUidToDelete) =
            getUidOutdated(
                SharingDataType.USER_GROUP,
                summary.userGroups.associate { it.id to it.revision })
        logD { "Pending user group updates: $userGroupUidToUpdate. deletions: $userGroupUidToDelete" }

        requestUpdate(
            session,
            IdCollection(itemUidToUpdate, itemGroupUidToUpdate, userGroupUidToUpdate),
            IdCollection(itemUidToDelete, itemGroupUidToDelete, userGroupUidToDelete)
        )

        logD { "Sending local changes" }
        sendItemsLocalUpdates(session, summary)
    }

    private suspend fun requestUpdate(
        session: Authorization.User,
        idsToRequest: IdCollection,
        idsToDelete: IdCollection
    ) {
        if (idsToRequest.isNotEmpty()) {
            logD { "Downloading updated content" }
            val (itemGroups, itemContents, userGroupDownloads) = sharingCommunicator.get(
                session,
                idsToRequest.items,
                idsToRequest.itemGroups,
                idsToRequest.userGroups
            )
            logD { "Applying remote updates & deletions" }
            updateItemLocally(
                itemGroups = itemGroups,
                itemContents = itemContents,
                userGroupDownloads = userGroupDownloads,
                idsToDelete = idsToDelete
            )
        } else if (idsToDelete.isNotEmpty()) {
            logD { "Applying remote deletions" }
            updateItemLocally(
                idsToDelete = idsToDelete
            )
        }
    }

    private suspend fun updateItemLocally(
        itemGroups: List<ItemGroup> = emptyList(),
        itemContents: List<ItemContent> = emptyList(),
        userGroupDownloads: List<UserGroup> = emptyList(),
        idsToDelete: IdCollection
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
        logD { "Applied remote changes locally" }
    }

    private suspend fun sendItemsLocalUpdates(
        session: Authorization.User,
        summary: SyncDownloadService.Data.SharingSummary
    ) {
        logV { "Loading local changes for upload" }
        val itemToUpdateList = sharingSyncDao.getDirtyForSharing().mapNotNull { dataIdentifier ->
            getItemToUpdateFrom(dataIdentifier, summary)
        }
        logV { "Found ${itemToUpdateList.size} local shared changes to upload" }
        if (itemToUpdateList.isNotEmpty()) {
            val itemContentSent = sharingCommunicator.updateItems(session, itemToUpdateList)
            for (itemContent in itemContentSent) {
                val itemId = itemContent.itemId
                sharingSyncDao.updateItemTimestamp(itemId, itemContent.timestamp.toLong())
            }
            sharingSyncDao.markAsShared(itemContentSent.map { it.itemId })
            logD { "Successfully uploaded local changes" }
        }
    }

    private suspend fun getItemToUpdateFrom(
        dataIdentifier: DataIdentifierExtraDataWrapper<out SyncObject>,
        summary: SyncDownloadService.Data.SharingSummary
    ): ItemToUpdate? {
        val uid = dataIdentifier.vaultItem.uid
        val syncTimestamp = summary.items.firstOrNull { it.id == uid }?.timestamp
        val (itemKeyBase64, remoteTimestamp) = sharingSyncDao.getItemKeyTimestamp(uid) ?: run {
            logW { "Failed to load shared item $dataIdentifier" }
            return null
        }
        
        
        
        val timestamp = syncTimestamp ?: remoteTimestamp
        val content = xmlConverter.toXml(dataIdentifier)
        val itemKey = itemKeyBase64.decodeBase64ToByteArrayOrNull()
        if (itemKey == null) {
            logW { "Failed to decipher key for $dataIdentifier" }
            return null
        }
        val contentEncrypted = sharingCryptographyHelper.encryptItemContent(content, itemKey) ?: return null
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

    private data class IdCollection(
        val items: List<String>,
        val itemGroups: List<String>,
        val userGroups: List<String>
    ) {
        fun isNotEmpty() =
            items.isNotEmpty() || itemGroups.isNotEmpty() || userGroups.isNotEmpty()
    }
}
