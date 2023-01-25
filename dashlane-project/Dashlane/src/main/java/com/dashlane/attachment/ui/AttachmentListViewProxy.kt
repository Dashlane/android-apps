package com.dashlane.attachment.ui

import android.app.DownloadManager
import android.content.Intent
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.R
import com.dashlane.attachment.AttachmentListContract
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.getThemeAttrColor
import com.google.android.material.snackbar.Snackbar
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import com.skocken.presentation.viewproxy.BaseViewProxy

class AttachmentListViewProxy(val activity: DashlaneActivity) :
    BaseViewProxy<AttachmentListContract.Presenter>(activity), AttachmentListContract.ViewProxy {

    override var selectedAttachments = 0
    private val adapter = AttachmentAdapter()
    private val recyclerView: RecyclerView = activity.findViewById(R.id.attachment_list)
    private val coordinatorLayout: CoordinatorLayout = activity.findViewById(R.id.attachment_list_coordinator_layout)
    override val itemSize: Int
        get() = adapter.size()

    init {
        setupListeners()
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            
            
            itemAnimator = null
            adapter = this@AttachmentListViewProxy.adapter
        }
    }

    override fun showAttachmentsList(attachments: List<AttachmentItem>) {
        adapter.clear()
        adapter.addAll(attachments)
    }

    override fun updateActionBar(barColor: Int, attachments: List<AttachmentItem>) {
        if (selectedAttachments == 0) {
            setupActionBar(barColor, attachments)
        } else {
            setupAttachmentSelectedActionBar(selectedAttachments)
        }
    }

    override fun showFileDeleted(secureFileInfoId: String) {
        val adapter = recyclerView.adapter as EfficientAdapter<*>
        val index = adapter.objects.indexOfFirst { it is AttachmentItem && it.id == secureFileInfoId }
        if (index == -1) {
            return
        }
        adapter.removeAt(index)
    }

    override fun showFileDeleteError(secureFileInfoId: String) {
        val adapter = recyclerView.adapter as EfficientAdapter<*>
        val filename =
            (adapter.objects.first { it is AttachmentItem && it.id == secureFileInfoId } as AttachmentItem).filename
        DialogHelper()
            .builder(context)
            .setMessage(context.getString(R.string.delete_file_error, filename))
            .setTitle(R.string.dashlane_main_app_name)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setCancelable(true).show()
    }

    private fun setupListeners() {
        adapter.onItemClickListener = EfficientAdapter.OnItemClickListener<AttachmentItem> { _, _, item, position ->
            if (selectedAttachments > 0) {
                toggleAttachmentSelection(item!!, position)
                return@OnItemClickListener
            }
            presenter.downloadOrOpenAttachment(item!!)
        }
        adapter.onItemLongClickListener =
            EfficientAdapter.OnItemLongClickListener<AttachmentItem> { _, _, item, position ->
                toggleAttachmentSelection(item!!, position)
            }
        adapter.onIconClickListener = object : AttachmentViewHolder.OnIconClickListener {
            override fun onIconClicked(item: AttachmentItem, position: Int) {
                toggleAttachmentSelection(item, position)
            }
        }
        adapter.onAttachmentListChangesListener = object : AttachmentAdapter.OnAttachmentListChangesListener {
            override fun onItemAdded(item: AttachmentItem) {
                presenter.addAttachment(item)
            }

            override fun onItemRemoved(item: AttachmentItem) {
                presenter.removeAttachment(item)
            }
        }
    }

    private fun setupActionBar(barColor: Int, attachments: List<AttachmentItem>) {
        resetAttachmentSelection(attachments)
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = activity.getString(R.string.attachments_activity_title)
            if (barColor != -1) {
                activity.actionBarUtil.setActionBarColor(barColor)
            }
        }
    }

    private fun setupAttachmentSelectedActionBar(selectedAttachments: Int) {
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = resources.getQuantityString(
                R.plurals.file_selected_quantity,
                selectedAttachments,
                selectedAttachments
            )
            activity.actionBarUtil.setActionBarColor(context.getThemeAttrColor(R.attr.colorSurface))
        }
    }

    private fun resetAttachmentSelection(attachments: List<AttachmentItem>) {
        for (attachment in attachments) {
            attachment.selected = false
        }
        recyclerView.adapter!!.notifyDataSetChanged()
    }

    private fun toggleAttachmentSelection(item: AttachmentItem, position: Int) {
        item.selected = !item.selected
        selectedAttachments += if (item.selected) 1 else -1
        recyclerView.adapter!!.notifyItemChanged(position)
        activity.invalidateOptionsMenu()
    }

    @Suppress("WrongConstant")
    override fun showExportDone() {
        SnackbarUtils.showSnackbar(
            coordinatorLayout,
            context.getString(R.string.export_public_folder_done),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(R.string.open_public_folder) {
                
                activity.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
            }
        }
    }

    override fun showDecryptionError() {
        showError(R.string.decryption_error)
    }

    override fun showExportError() {
        showError(R.string.export_error)
    }

    override fun showExportStorageError() {
        showError(R.string.storage_error)
    }

    private fun showError(@StringRes exportError: Int) {
        SnackbarUtils.showSnackbar(
            coordinatorLayout,
            context.getString(exportError),
            Snackbar.LENGTH_LONG
        )
    }

    @Suppress("WrongConstant")
    override fun showPermissionError() {
        SnackbarUtils.showPermissionSnackbar(
            coordinatorLayout,
            SnackbarUtils.getStoragePermissionText(context),
            Snackbar.LENGTH_INDEFINITE
        )
    }
}
