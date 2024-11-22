package com.dashlane.attachment

import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.dashlane.attachment.ui.AttachmentItem
import com.skocken.presentation.definition.Base
import java.io.File

interface AttachmentListContract {

    interface ViewProxy : Base.IView {
        var selectedAttachments: Int
        val itemSize: Int
        fun updateActionBar(attachments: List<AttachmentItem>)
        fun showAttachmentsList(attachments: List<AttachmentItem>)
        fun showFileDeleted(secureFileInfoId: String)
        fun showFileDeleteError(secureFileInfoId: String)
        fun showExportDone()
        fun showPermissionError()
        fun showDecryptionError()
        fun showExportError()
        fun showExportStorageError()
    }

    interface DataProvider : Base.IDataProvider {
        val attachments: List<AttachmentItem>

        suspend fun setup()

        suspend fun getDecipheredFile(attachmentItem: AttachmentItem): File

        suspend fun writeDecipheredFileToPublicFolder(attachmentItem: AttachmentItem)

        suspend fun deleteDecipheredFileCache()

        suspend fun addAttachment(attachmentItem: AttachmentItem)
        suspend fun removeAttachment(attachmentItem: AttachmentItem)
    }

    interface Presenter : Base.IPresenter {

        var isAttachmentListUpdated: Boolean

        fun onCreate()

        fun onListLoaded(attachments: MutableList<AttachmentItem>)

        fun onOpenDocumentResult(uri: Uri?)

        fun onResume()

        fun onCreateOptionsMenu(inflater: MenuInflater, menu: Menu)

        fun onOptionsItemSelected(item: MenuItem): Boolean

        fun downloadOrOpenAttachment(item: AttachmentItem)

        fun addAttachment(item: AttachmentItem)

        fun removeAttachment(item: AttachmentItem)
    }
}
