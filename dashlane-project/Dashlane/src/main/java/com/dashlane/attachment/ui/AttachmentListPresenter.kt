package com.dashlane.attachment.ui

import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.dashlane.R
import com.dashlane.attachment.AttachmentListContract
import com.dashlane.attachment.ui.AttachmentItem.DownloadState.DOWNLOADED
import com.dashlane.attachment.ui.AttachmentItem.DownloadState.NOT_DOWNLOADED
import com.dashlane.cryptography.CryptographyException
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.lock.LockHelper
import com.dashlane.securefile.Attachment
import com.dashlane.securefile.DeleteFileManager
import com.dashlane.securefile.DownloadFileContract
import com.dashlane.securefile.UploadFileContract
import com.dashlane.securefile.extensions.toSecureFile
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.UriUtils
import com.dashlane.vault.item.VaultItemLogAttachmentHelper
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.IOException

class AttachmentListPresenter(
    private val coroutineScope: CoroutineScope,
    private val uploadPresenter: UploadFileContract.Presenter,
    private val downloadPresenter: DownloadFileContract.Presenter,
    private val deleteManager: DeleteFileManager,
    private val lockHelper: LockHelper,
    private val vaultItemLogAttachmentHelper: VaultItemLogAttachmentHelper
) : AttachmentListContract.Presenter,
    BasePresenter<AttachmentListContract.DataProvider, AttachmentListContract.ViewProxy>() {

    override var isAttachmentListUpdated = false
    private var isFirstSearchLaunched = false

    override fun onCreate() {
        view.updateActionBar(provider.attachments)
    }

    override fun onListLoaded(attachments: MutableList<AttachmentItem>) {
        view.showAttachmentsList(attachments)
    }

    override fun onResume() {
        if (!isAttachmentListUpdated && !isFirstSearchLaunched && provider.attachments.isEmpty()) {
            isFirstSearchLaunched = true
            
            uploadPresenter.performFileSearch()
            return
        }
        coroutineScope.launch(Dispatchers.Main) {
            
            provider.setup()
            
            provider.deleteDecipheredFileCache()
        }
    }

    override fun onOpenDocumentResult(uri: Uri?) {
        if (uri != null) {
            uploadPresenter.notifyFileChosen(uri)
            isAttachmentListUpdated = true
        } else if (provider.attachments.isEmpty()) {
            activity?.finish()
        }
    }

    override fun onCreateOptionsMenu(inflater: MenuInflater, menu: Menu) {
        if (view.selectedAttachments > 0) {
            inflater.inflate(R.menu.delete_menu, menu)
        }
        view.updateActionBar(provider.attachments)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (view.selectedAttachments > 0) {
                    view.selectedAttachments = 0
                    view.updateActionBar(provider.attachments)
                    activity?.invalidateOptionsMenu()
                    return true
                }
                activity?.finish()
                return true
            }
            R.id.menu_delete -> {
                val selectedAttachments = provider.attachments.filter { it.selected }
                askForDeleteConfirmation(selectedAttachments)
                return true
            }
            else -> return false
        }
    }

    override fun addAttachment(item: AttachmentItem) {
        coroutineScope.launch(Dispatchers.Main) { provider.addAttachment(item) }
    }

    override fun removeAttachment(item: AttachmentItem) {
        coroutineScope.launch(Dispatchers.Main) {
            provider.removeAttachment(item)
            
            if (view.itemSize == 0) {
                activity?.finish()
            }
        }
    }

    override fun downloadOrOpenAttachment(item: AttachmentItem) {
        when (item.downloadState) {
            DOWNLOADED -> askForActionOnAttachment(item)
            NOT_DOWNLOADED -> downloadPresenter.downloadAttachment(item.attachment)
            else -> Unit
        }
    }

    private fun askForActionOnAttachment(item: AttachmentItem) {
        val activity = activity ?: return

        val items = listOf(
            R.string.open_file_action,
            R.string.export_file_action,
            R.string.delete_file_action
        ).map { activity.getString(it) }.toTypedArray<CharSequence>()

        DialogHelper()
            .builder(context!!)
            .setTitle(item.attachment.filename)
            .setItems(items) { _, which ->
                when (which) {
                    
                    0 -> openAttachmentFile(item)
                    
                    1 -> askForExport(item)
                    
                    2 -> askForDeleteConfirmation(listOf(item))
                }
            }
            .setCancelable(true)
            .show()
    }

    private fun openAttachmentFile(item: AttachmentItem) {
        val activity = activity ?: return

        coroutineScope.launch(Dispatchers.Main) {
            val file = try {
                provider.getDecipheredFile(item)
            } catch (e: IllegalArgumentException) {
                
                
                view.showDecryptionError()
                return@launch
            } catch (e: CryptographyException) {
                view.showDecryptionError()
                return@launch
            } catch (e: IOException) {
                view.showExportError()
                return@launch
            }

            
            lockHelper.startAutoLockGracePeriod()

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    UriUtils.getOpenFileUri(activity.applicationContext, file),
                    item.attachment.type
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            downloadPresenter.onAttachmentOpened(item.attachment)
            if (openIntent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(openIntent)
            } else {
                DialogHelper()
                    .builder(context!!)
                    .setTitle(item.attachment.filename)
                    .setMessage(context!!.getString(R.string.file_open_error, item.attachment.filename))
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(true)
                    .show()
            }
        }
    }

    private fun exportAttachmentFile(item: AttachmentItem) {
        vaultItemLogAttachmentHelper.logDownload()
        coroutineScope.launch(Dispatchers.Main) {
            try {
                provider.writeDecipheredFileToPublicFolder(item)
                view.showExportDone()
            } catch (e: IllegalArgumentException) {
                
                view.showExportStorageError()
            } catch (e: CryptographyException) {
                view.showDecryptionError()
            } catch (e: IOException) {
                view.showExportError()
            }
        }
    }

    private fun askForExport(item: AttachmentItem) {
        DialogHelper()
            .builder(context!!).apply {
                setMessage(context.getString(R.string.export_file_confirm, item.attachment.filename))
                setTitle(R.string.export_file_confirm_title)
                setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    exportAttachmentFile(item)
                }
                setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                setCancelable(false)
            }.show()
    }

    private fun askForDeleteConfirmation(selectedAttachments: List<AttachmentItem>) {
        DialogHelper()
            .builder(context!!).apply {
                if (selectedAttachments.size == 1) {
                    setMessage(context.getString(R.string.delete_file_confirm, selectedAttachments[0].attachment.filename))
                } else {
                    setMessage(
                        context.getString(
                            R.string.delete_files_confirm,
                            selectedAttachments.size.toString()
                        )
                    )
                }
                setTitle(R.string.delete_file_confirm_title)
                setPositiveButton(R.string.delete) { dialog, _ ->
                    dialog.dismiss()
                    selectedAttachments.forEach { deleteFile(it.attachment) }
                    view.selectedAttachments = 0
                    view.updateActionBar(provider.attachments)
                    activity?.invalidateOptionsMenu()
                }
                setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                setCancelable(false)
            }.show()
    }

    private fun deleteFile(attachment: Attachment) {
        attachment.id?.let {
            coroutineScope.launch(Dispatchers.Main) {
                if (deleteManager.deleteSecureFile(it, attachment.toSecureFile())) {
                    vaultItemLogAttachmentHelper.logUpdate(Action.DELETE)
                    isAttachmentListUpdated = true
                    view.showFileDeleted(it)
                } else {
                    view.showFileDeleteError(it)
                }
            }
        }
    }
}
