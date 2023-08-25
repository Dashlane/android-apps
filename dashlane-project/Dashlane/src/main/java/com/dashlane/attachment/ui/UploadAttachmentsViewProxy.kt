@file:Suppress("DEPRECATION")

package com.dashlane.attachment.ui

import android.app.ProgressDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.attachment.extensions.toAttachmentItem
import com.dashlane.securefile.SecureFile
import com.dashlane.securefile.UploadFileContract
import com.dashlane.ui.util.DialogHelper
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import com.skocken.presentation.viewproxy.BaseViewProxy



class UploadAttachmentsViewProxy(val activity: AppCompatActivity) :
    BaseViewProxy<UploadFileContract.Presenter>(activity),
    UploadFileContract.ViewProxy {

    private val addAttachmentButton =
        findViewByIdEfficient<FloatingActionButton>(R.id.data_list_floating_button)!!
    private val recyclerView = findViewByIdEfficient<RecyclerView>(R.id.attachment_list)!!
    private var progressDialog: ProgressDialog? = null

    init {
        addAttachmentButton.setOnClickListener { presenter.performFileSearch() }
    }

    override fun showRetrievingFile(fileName: String) {
        progressDialog = ProgressDialog(context).apply {
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setTitle(context.getString(R.string.retrieve_file_dialog_title))
            setMessage(context.getString(R.string.retrieve_file_dialog_message, fileName))
        }
        progressDialog!!.show()
    }

    override fun showStartUpload(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>) {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog(context).apply {
            setCancelable(false)
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
            setTitle(context.getString(R.string.upload_file_dialog_title))
            setMessage(secureFile.fileName)
        }
        progressDialog!!.show()
    }

    @Suppress("UNCHECKED_CAST")
    override fun showUploadedFile(secureFile: SecureFile, secureFileInfo: VaultItem<SyncObject.SecureFileInfo>) {
        progressDialog?.dismiss()
        val adapter = recyclerView.adapter as EfficientAdapter<AttachmentItem>
        adapter.add(secureFileInfo.syncObject.toAttachmentItem())
    }

    override fun showProgress(sizeUploaded: Long, totalSize: Long) {
        val uploadProgress = ((sizeUploaded.toFloat() / totalSize.toFloat()) * 100).toInt()
        progressDialog?.progress = uploadProgress
    }

    override fun showError(errorMessageLocalized: String) {
        progressDialog?.dismiss()
        DialogHelper()
            .builder(context)
            .setMessage(errorMessageLocalized)
            .setTitle(R.string.dashlane_main_app_name)
            .setPositiveButton(
                android.R.string.ok,
                object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            }
            )
            .setCancelable(true).show()
    }
}
