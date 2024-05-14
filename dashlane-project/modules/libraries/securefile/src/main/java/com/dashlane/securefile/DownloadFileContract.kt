package com.dashlane.securefile

import com.skocken.presentation.definition.Base

interface DownloadFileContract {
    interface DataProvider : Base.IDataProvider {
        suspend fun downloadSecureFile(attachment: Attachment)
    }

    interface Presenter : Base.IPresenter {
        fun downloadAttachment(attachment: Attachment)

        fun notifyFileDownloaded(attachment: Attachment, secureFileInfoAnonymousId: String?)
        fun notifyFileDownloadError(attachment: Attachment, secureFileInfoAnonymousId: String?, t: Throwable)
        fun notifyFileAccessError(attachment: Attachment, secureFileInfoAnonymousId: String?)
        fun notifyFileDownloadProgress(attachment: Attachment, secureFileInfoAnonymousId: String?, progress: Float)
        fun onAttachmentOpened(attachment: Attachment)
    }

    interface ViewProxy : Base.IView {
        fun showFileDownloaded(attachment: Attachment)
        fun showProgress(attachment: Attachment, progress: Float)
        fun showError(attachment: Attachment, errorMessageLocalized: String)
        fun showGenericError(errorMessageLocalized: String)
    }
}