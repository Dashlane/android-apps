package com.dashlane.attachment.ui

import com.dashlane.R
import com.dashlane.attachment.VaultItemLogAttachmentHelper
import com.dashlane.securefile.Attachment
import com.dashlane.securefile.DownloadFileContract
import com.dashlane.securefile.FileSecurity
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DownloadAttachmentsPresenter(
    private val coroutineScope: CoroutineScope,
    private val vaultItemLogAttachmentHelper: VaultItemLogAttachmentHelper
) : BasePresenter<DownloadFileContract.DataProvider, DownloadFileContract.ViewProxy>(),
    DownloadFileContract.Presenter {

    override fun downloadAttachment(attachment: Attachment) {
        val context = context ?: return

        if (!attachment.isSupportedVersion()) {
            view.showGenericError(context.getString(R.string.file_version_error))
            return
        }

        if (!FileSecurity.isSupportedType(attachment.filename, attachment.type)) {
            view.showGenericError(context.getString(R.string.file_type_error_download))
            return
        }

        viewOrNull?.showProgress(attachment, 0F)

        coroutineScope.launch(Dispatchers.Main) {
            provider.downloadSecureFile(attachment)
        }
    }

    override fun notifyFileDownloaded(attachment: Attachment, secureFileInfoAnonymousId: String?) {
        viewOrNull?.showFileDownloaded(attachment)
    }

    override fun notifyFileDownloadError(attachment: Attachment, secureFileInfoAnonymousId: String?, t: Throwable) {
        val context = context ?: return

        viewOrNull?.showError(
            attachment,
            context.getString(R.string.download_file_generic_error, attachment.filename)
        )
    }

    override fun notifyFileAccessError(attachment: Attachment, secureFileInfoAnonymousId: String?) {
        val context = context ?: return

        viewOrNull?.showError(
            attachment,
            context.getString(R.string.download_file_access_error)
        )
    }

    override fun notifyFileDownloadProgress(
        attachment: Attachment,
        secureFileInfoAnonymousId: String?,
        progress: Float
    ) {
        viewOrNull?.showProgress(attachment, progress)
    }

    override fun onAttachmentOpened(attachment: Attachment) {
        coroutineScope.launch(Dispatchers.Main) {
            vaultItemLogAttachmentHelper.logView()
        }
    }
}