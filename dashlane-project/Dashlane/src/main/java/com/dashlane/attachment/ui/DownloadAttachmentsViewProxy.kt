package com.dashlane.attachment.ui

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.securefile.Attachment
import com.dashlane.securefile.DownloadFileContract
import com.dashlane.ui.util.DialogHelper
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import com.skocken.presentation.viewproxy.BaseViewProxy

class DownloadAttachmentsViewProxy(activity: Activity) :
    BaseViewProxy<DownloadFileContract.Presenter>(activity),
    DownloadFileContract.ViewProxy {

    private val recyclerView = findViewByIdEfficient<RecyclerView>(R.id.attachment_list)!!

    override fun showFileDownloaded(attachment: Attachment) {
        updateAttachment(attachment, AttachmentItem.DownloadState.DOWNLOADED, 100)
    }

    override fun showProgress(attachment: Attachment, progress: Float) {
        updateAttachment(attachment, AttachmentItem.DownloadState.DOWNLOADING, progress.toInt())
    }

    override fun showError(attachment: Attachment, errorMessageLocalized: String) {
        updateAttachment(attachment, AttachmentItem.DownloadState.NOT_DOWNLOADED, 0)
        showErrorDialog(errorMessageLocalized)
    }

    override fun showGenericError(errorMessageLocalized: String) {
        showErrorDialog(errorMessageLocalized)
    }

    private fun showErrorDialog(error: String) {
        DialogHelper()
            .builder(context)
            .setTitle(R.string.dashlane_main_app_name)
            .setMessage(error)
            .setPositiveButton(android.R.string.ok, null)
            .setCancelable(true)
            .show()
    }

    private fun updateAttachment(
        attachment: Attachment,
        state: AttachmentItem.DownloadState,
        progress: Int
    ) {
        val adapter = recyclerView.adapter as EfficientAdapter<*>
        val index = adapter.objects.indexOfFirst { it is AttachmentItem && it.attachment.downloadKey == attachment.downloadKey }
        if (index == -1) {
            return
        }
        (adapter.objects[index] as AttachmentItem).apply {
            downloadState = state
            downloadProgress = progress
            recyclerView.adapter!!.notifyItemChanged(index)
        }
    }
}
