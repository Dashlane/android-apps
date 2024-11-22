package com.dashlane.securefile

import com.skocken.presentation.definition.Base

interface DownloadFileContract {
    interface DataProvider : Base.IDataProvider {
        suspend fun downloadSecureFile(attachment: Attachment)
    }

    interface Presenter : Base.IPresenter {
        fun downloadAttachment(attachment: Attachment)

        fun notifyFileDownloaded(attachment: Attachment)
        fun notifyFileDownloadError(attachment: Attachment, t: Throwable)
        fun notifyFileAccessError(attachment: Attachment)
        fun notifyFileDownloadProgress(attachment: Attachment, progress: Float)
        fun onAttachmentOpened(attachment: Attachment)
    }

    interface ViewProxy : Base.IView {
        fun showFileDownloaded(attachment: Attachment)
        fun showProgress(attachment: Attachment, progress: Float)
        fun showError(attachment: Attachment, errorMessageLocalized: String)
        fun showGenericError(errorMessageLocalized: String)
    }
}