package com.dashlane.attachment

import android.Manifest
import androidx.annotation.RequiresPermission
import com.dashlane.attachment.ui.AttachmentItem
import com.dashlane.core.DataSync
import com.dashlane.securefile.extensions.toSecureFile
import com.dashlane.securefile.storage.SecureFileStorage
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.useractivity.log.usage.UsageLogCode134
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
    private val secureFileStorage: SecureFileStorage
) : BaseDataProvider<AttachmentListContract.Presenter>(),
    AttachmentListContract.DataProvider {

    override val attachments = AttachmentsParser().parse(jsonAttachments).toMutableList()

    override suspend fun setup() {
        secureFileStorage.init()
        attachments.onEach {
            it.downloadState = if (secureFileStorage.isDownloaded(it.toSecureFile(), it.remoteSize)) {
                AttachmentItem.DownloadState.DOWNLOADED
            } else {
                AttachmentItem.DownloadState.NOT_DOWNLOADED
            }
        }
        presenter.onListLoaded(attachments)
    }

    override suspend fun getDecipheredFile(attachmentItem: AttachmentItem): File {
        return secureFileStorage.decipherToFileProvider(attachmentItem.toSecureFile())
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, conditional = true)
    override suspend fun writeDecipheredFileToPublicFolder(attachmentItem: AttachmentItem) {
        secureFileStorage.decipherToPublicFolder(
            attachmentItem.toSecureFile(),
            attachmentItem.type!!,
            attachmentItem.localSize
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
            attachments = Gson().toJson(this@AttachmentListDataProvider.attachments)
            syncState = SyncState.MODIFIED
            userModificationDate = Instant.now()
        }
        dataSaver.save(updated)

        DataSync.sync(UsageLogCode134.Origin.SAVE)
    }
}
