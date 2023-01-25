package com.dashlane.item.delete

import com.dashlane.attachment.AttachmentsParser
import com.dashlane.attachment.VaultItemLogAttachmentHelper
import com.dashlane.core.DataSync
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.securefile.DeleteFileManager
import com.dashlane.securefile.extensions.toSecureFile
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.useractivity.log.usage.UsageLogCode134
import com.dashlane.util.tryOrNull
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.toItemType
import com.dashlane.vault.util.getTeamSpaceLog
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteVaultItemProvider @Inject constructor(
    mainDataAccessor: MainDataAccessor,
    private val vaultItemLogger: VaultItemLogger,
    private val deleteFileManager: DeleteFileManager,
    private val sharingPolicy: SharingPolicyDataProvider
) : BaseDataProvider<DeleteVaultItemContract.Presenter>(), DeleteVaultItemContract.DataProvider {

    private val dataQuery = mainDataAccessor.getVaultDataQuery()
    private val dataSaver = mainDataAccessor.getDataSaver()

    override suspend fun deleteItem(itemId: String): Boolean {
        val itemFilter = vaultFilter {
            specificUid(itemId)
        }
        dataQuery.query(itemFilter)?.let { item ->
            val deletedItem = item.copyWithAttrs { syncState = SyncState.DELETED }
            val isDeleted = if (item.isShared()) {
                
                revokeSharingAccess(item)
            } else {
                deleteAttachments(item) && saveItem(deletedItem)
            }

            return if (isDeleted) {
                vaultItemLogger.logUpdate(
                    action = Action.DELETE,
                    editedFields = null,
                    itemId = item.uid,
                    itemType = item.syncObjectType.toItemType(),
                    space = item.getTeamSpaceLog(),
                    url = (item.syncObject as? SyncObject.Authentifiant)?.urlForGoToWebsite,
                    addedWebsites = null,
                    removedWebsites = null,
                    removedApps = null
                )

                
                DataSync.sync(UsageLogCode134.Origin.SAVE)
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

    private suspend fun saveItem(data: VaultItem<*>) =
        withContext(Dispatchers.Default) { tryOrNull { dataSaver.save(data) } ?: false }

    private fun attachmentDeleted(item: VaultItem<*>, secureFileInfoId: String) {
        VaultItemLogAttachmentHelper(vaultItemLogger, item).logUpdate(Action.DELETE)
        val updatedAttachments =
            AttachmentsParser().parse(item.syncObject.attachments).toMutableList()
        
        updatedAttachments.removeAll { it.id == secureFileInfoId }
    }
}