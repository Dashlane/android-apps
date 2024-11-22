package com.dashlane.vault.item.delete

import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.securefile.AttachmentsParser
import com.dashlane.securefile.DeleteFileManager
import com.dashlane.securefile.extensions.toSecureFile
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.CollectionDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.collectionFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.getTeamSpaceLog
import com.dashlane.util.tryOrNull
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.item.VaultItemLogAttachmentHelper
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummaryOrNull
import com.dashlane.vault.toItemType
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteVaultItemProvider @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val collectionDataQuery: CollectionDataQuery,
    private val dataSaver: DataSaver,
    private val vaultItemLogger: VaultItemLogger,
    private val activityLogger: VaultActivityLogger,
    private val deleteFileManager: DeleteFileManager,
    private val sharingPolicy: SharingPolicyDataProvider,
    private val dataSync: DataSync
) {

    suspend fun deleteItem(itemId: String): Boolean {
        val itemFilter = vaultFilter {
            specificUid(itemId)
        }
        val removedFromCollections: List<VaultItem<*>> = collectionDataQuery.queryByIds(
            collectionDataQuery.queryAll(
                collectionFilter {
                    withVaultItemId = itemId
                }
            ).map { it.id }
        ).map { syncCollection ->
            syncCollection.copy(
                syncObject = syncCollection.syncObject.copy {
                    vaultItems = vaultItems?.filterNot { it.id == itemId }
                }
            )
        }

        vaultDataQuery.query(itemFilter)?.let { item ->
            val deletedItem = item.copyWithAttrs { syncState = SyncState.DELETED }
            val isDeleted = if (item.isShared()) {
                saveItems(removedFromCollections)
                
                revokeSharingAccess(item)
            } else {
                deleteAttachments(item) && saveItems(removedFromCollections + deletedItem)
            }

            return if (isDeleted) {
                val action = Action.DELETE
                vaultItemLogger.logUpdate(
                    action = action,
                    editedFields = null,
                    itemId = item.uid,
                    itemType = item.syncObjectType.toItemType(),
                    space = item.getTeamSpaceLog(),
                    url = item.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)
                        ?.toSummaryOrNull<SummaryObject.Authentifiant>()?.urlForGoToWebsite,
                    addedWebsites = null,
                    removedWebsites = null,
                    removedApps = null
                )
                activityLogger.sendAuthentifiantActivityLog(vaultItem = item, action = action)
                
                dataSync.sync(Trigger.SAVE)
                true
            } else {
                false
            }
        }
        return false
    }

    private suspend fun revokeSharingAccess(item: VaultItem<*>): Boolean {
        return sharingPolicy.doCancelSharingFor(item)
    }

    private suspend fun deleteAttachments(item: VaultItem<*>): Boolean {
        val attachments = AttachmentsParser().parse(item.syncObject.attachments)
        var allAttachmentDeleted = true
        if (attachments.isNotEmpty()) {
            
            attachments.forEach {
                if (deleteFileManager.deleteSecureFile(it.id!!, it.toSecureFile())) {
                    attachmentDeleted(item, it.id!!)
                } else {
                    allAttachmentDeleted = false
                    return@forEach
                }
            }
        }
        return allAttachmentDeleted
    }

    private suspend fun saveItems(data: List<VaultItem<*>>) =
        withContext(Dispatchers.Default) { tryOrNull { dataSaver.save(data) } ?: false }

    private fun attachmentDeleted(item: VaultItem<*>, secureFileInfoId: String) {
        VaultItemLogAttachmentHelper(vaultItemLogger, item).logUpdate(Action.DELETE)
        val updatedAttachments =
            AttachmentsParser().parse(item.syncObject.attachments).toMutableList()
        
        updatedAttachments.removeAll { it.id == secureFileInfoId }
    }
}