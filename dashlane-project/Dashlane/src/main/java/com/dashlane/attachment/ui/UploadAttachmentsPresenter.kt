package com.dashlane.attachment.ui

import android.content.ActivityNotFoundException
import android.net.Uri
import android.text.format.Formatter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import com.dashlane.R
import com.dashlane.attachment.VaultItemLogAttachmentHelper
import com.dashlane.cryptography.asEncryptedFile
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.lock.LockHelper
import com.dashlane.securefile.FileSecurity
import com.dashlane.securefile.SecureFile
import com.dashlane.securefile.UploadFileContract
import com.dashlane.securefile.extensions.getFileName
import com.dashlane.securefile.extensions.getFileSize
import com.dashlane.securefile.extensions.getFileType
import com.dashlane.securefile.extensions.toSecureFileInfo
import com.dashlane.session.SessionManager
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration

class UploadAttachmentsPresenter(
    private val userFeaturesChecker: UserFeaturesChecker,
    private val lockHelper: LockHelper,
    private val coroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val vaultItemLogAttachmentHelper: VaultItemLogAttachmentHelper,
    private val openDocumentResultLauncher: ActivityResultLauncher<Unit>
) : BasePresenter<UploadFileContract.DataProvider, UploadFileContract.ViewProxy>(), UploadFileContract.Presenter {

    private var lockedFileUpload: Uri? = null

    override fun notifyFileSpaceReserved(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>) {
        
        view.showStartUpload(secureFile, secureFileInfo)
    }

    override fun notifyFileUploaded(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>) {
        view.showUploadedFile(secureFile, secureFileInfo)
        vaultItemLogAttachmentHelper.logUpdate(Action.ADD)
    }

    override fun notifyFileUploadProgress(totalBytesUploaded: Long, contentLength: Long) {
        view.showProgress(totalBytesUploaded, contentLength)
    }

    override fun notifyStorageSpaceChanged(remainingSpaceBytes: Long, maxSpaceBytes: Long) {
        
    }

    override fun notifyMaxStorageSpaceReached(
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>,
        code: Int,
        message: String
    ) {
        
        context?.apply { view.showError(getString(R.string.upload_file_no_space)) }
    }

    override fun notifyFileUploadFailed(
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        
        context?.apply { view.showError(getString(R.string.upload_file_error)) }
    }

    override fun notifyFileSizeLimitExceeded(
        secureFile: SecureFile,
        secureFileInfo: VaultItem<SyncObject.SecureFileInfo>
    ) {
        val context = context ?: return
        
        val maxSize = Formatter.formatShortFileSize(
            context,
            userFeaturesChecker
                .getFeatureInfo(UserFeaturesChecker.Capability.SECURE_FILES_UPLOAD)
                .optLong("maxFileSize")
        )
        view.showError(context.getString(R.string.file_too_big_error, maxSize))
    }

    override fun performFileSearch() {
        
        lockHelper.startAutoLockGracePeriod(Duration.ofMinutes(2))
        try {
            openDocumentResultLauncher.launch()
        } catch (e: ActivityNotFoundException) {
            
        }
    }

    override fun notifyFileChosen(uri: Uri) {
        if (lockHelper.isLocked) {
            lockedFileUpload = uri
            return
        }
        lockedFileUpload = null
        val currentContext = context ?: return
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val contentResolver = currentContext.contentResolver
                val lType = contentResolver.getFileType(uri)
                val filename = contentResolver.getFileName(uri) ?: currentContext.getString(R.string.file_no_name)
                if (!FileSecurity.isSupportedType(filename, lType)) {
                    view.showError(currentContext.getString(R.string.file_type_error_upload))
                    return@launch
                }
                
                
                val encryptedFile =
                    File.createTempFile("DashlaneSecureFile-", ".dsf", currentContext.cacheDir).asEncryptedFile()
                view.showRetrievingFile(filename)
                val inputStream =
                    withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(uri)
                    }!!
                val secureFile = provider.createSecureFile(
                    filename,
                    inputStream,
                    encryptedFile
                )
                val session = sessionManager.session!!
                val username = session.userId
                val uki = session.uki
                val fileSize = contentResolver.getFileSize(uri)
                
                val secureFileInfo = secureFile.toSecureFileInfo(username).copySyncObject {
                    type = lType
                    localSize = fileSize
                }
                
                provider.uploadSecureFile(username, uki, secureFile, secureFileInfo)
            } catch (e: Exception) {
                
                
                
                
                view.showError(currentContext.getString(R.string.upload_file_error))
            }
        }
    }

    override fun resumeLockedFileUpload() {
        if (lockedFileUpload != null) {
            notifyFileChosen(lockedFileUpload!!)
        }
    }
}
