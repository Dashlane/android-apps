package com.dashlane.securefile

import android.net.Uri
import com.dashlane.cryptography.EncryptedFile
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.definition.Base
import java.io.InputStream

interface UploadFileContract {
    interface DataProvider : Base.IDataProvider {
        suspend fun createSecureFile(
            filename: String,
            inputStream: InputStream,
            encryptedFile: EncryptedFile
        ): SecureFile

        suspend fun uploadSecureFile(
            username: String,
            uki: String,
            secureFile: SecureFile,
            secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
        )
    }

    interface Presenter : Base.IPresenter {
        fun notifyMaxStorageSpaceReached(
            secureFile: SecureFile,
            secureFileInfo: VaultItem<SyncObject.SecureFileInfo>,
            code: Int,
            message: String
        )

        fun notifyFileUploadFailed(
            secureFile: SecureFile,
            secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
        )

        fun notifyFileSizeLimitExceeded(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>)
        fun notifyFileSpaceReserved(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>)
        fun notifyFileUploaded(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>)
        fun notifyStorageSpaceChanged(remainingSpaceBytes: Long, maxSpaceBytes: Long)
        fun performFileSearch()
        fun notifyFileChosen(uri: Uri)
        fun notifyFileUploadProgress(totalBytesUploaded: Long, contentLength: Long)
        fun resumeLockedFileUpload()
    }

    interface ViewProxy : Base.IView {
        fun showUploadedFile(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>)
        fun showProgress(sizeUploaded: Long, totalSize: Long)
        fun showStartUpload(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>)
        fun showError(errorMessageLocalized: String)
        fun showRetrievingFile(fileName: String)
    }
}