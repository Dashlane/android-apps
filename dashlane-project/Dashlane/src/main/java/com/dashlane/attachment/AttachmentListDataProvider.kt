package com.dashlane.attachment

import com.dashlane.attachment.ui.AttachmentItem
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.securefile.AttachmentsParser
import com.dashlane.securefile.extensions.toSecureFile
import com.dashlane.securefile.storage.SecureFileStorage
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.sync.DataSync
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.google.gson.Gson
import com.skocken.presentation.provider.BaseDataProvider
import java.io.File
import java.time.Instant

class AttachmentListDataProvider(
    jsonAttachments: String?,
    val vaultItem: VaultItem<*>,
    private val dataSaver: DataSaver,
    private val dataSync: DataSync,
    private val secureFileStorage: SecureFileStorage
) : BaseDataProvider<AttachmentListContract.Presenter>(),
    AttachmentListContract.DataProvider {

    override val attachments = AttachmentsParser().parse(jsonAttachments)
        .map { AttachmentItem(attachment = it) }
        .toMutableList()

    override suspend fun setup() {
        secureFileStorage.init()
        attachments.onEach {
            it.downloadState =
                if (secureFileStorage.isDownloaded(it.attachment.toSecureFile(), it.attachment.remoteSize)) {
                    AttachmentItem.DownloadState.DOWNLOADED
                } else {
                    AttachmentItem.DownloadState.NOT_DOWNLOADED
                }
        }
        presenter.onListLoaded(attachments)
    }

    override suspend fun getDecipheredFile(attachmentItem: AttachmentItem): File {
        return secureFileStorage.decipherToFileProvider(attachmentItem.attachment.toSecureFile())
    }

    override suspend fun writeDecipheredFileToPublicFolder(attachmentItem: AttachmentItem) {
        secureFileStorage.decipherToPublicFolder(
            attachmentItem.attachment.toSecureFile(),
            attachmentItem.attachment.type!!,
            attachmentItem.attachment.localSize
        )
    }

    override suspend fun deleteDecipheredFileCache() = secureFileStorage.deleteDecipheredFilesCache()

    override suspend fun addAttachment(attachmentItem: AttachmentItem) {
        attachments.add(attachmentItem)
        updateDataIdentifier()
    }

    override suspend fun removeAttachment(attachmentItem: AttachmentItem) {
        attachments.remove(attachmentItem)
        updateDataIdentifier()
    }

    private suspend fun updateDataIdentifier() {
        val updated = vaultItem.copyWithAttrs {
            attachments = Gson().toJson(this@AttachmentListDataProvider.attachments.map { it.attachment })
            syncState = SyncState.MODIFIED
            userModificationDate = Instant.now()
        }
        dataSaver.save(updated)

        dataSync.sync(Trigger.SAVE)
    }
}
