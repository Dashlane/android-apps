package com.dashlane.attachment.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.announcements.AnnouncementCenter
import com.dashlane.attachment.AttachmentListContract
import com.dashlane.attachment.AttachmentListDataProvider
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.securefile.DeleteFileManager
import com.dashlane.securefile.DownloadFileContract
import com.dashlane.securefile.UploadFileContract
import com.dashlane.securefile.storage.SecureFileStorage
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.item.VaultItemLogAttachmentHelper
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObjectType.Companion.forXmlNameOrNull
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AttachmentListActivity : DashlaneActivity() {
    @Inject
    lateinit var mSessionManager: SessionManager

    @Inject
    lateinit var vaultDataQuery: VaultDataQuery

    @Inject
    lateinit var dataSaver: DataSaver

    @Inject
    lateinit var dataSync: DataSync

    @Inject
    lateinit var uploadFileDataProvider: UploadFileContract.DataProvider

    @Inject
    lateinit var downloadFileDataProvider: DownloadFileContract.DataProvider

    @Inject
    lateinit var secureFileStorage: SecureFileStorage

    @Inject
    lateinit var vaultItemLogger: VaultItemLogger

    @Inject
    lateinit var deleteFileManager: DeleteFileManager

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    @Inject
    lateinit var announcementCenter: AnnouncementCenter

    private var attachmentListProvider: AttachmentListContract.DataProvider? = null

    private lateinit var attachmentListPresenter: AttachmentListPresenter
    private lateinit var uploadAttachmentsPresenter: UploadAttachmentsPresenter

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        
        if (::attachmentListPresenter.isInitialized) {
            attachmentListPresenter.onCreateOptionsMenu(menuInflater, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onApplicationUnlocked() {
        super.onApplicationUnlocked()
        
        uploadAttachmentsPresenter.resumeLockedFileUpload()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return attachmentListPresenter.onOptionsItemSelected(item)
    }

    override fun finish() {
        if (attachmentListProvider != null && ::attachmentListPresenter.isInitialized) {
            setAttachmentsResult(attachmentListPresenter)
        } else {
            
            setResult(RESULT_CANCELED)
        }
        super.finish()
    }

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_attachment_list)
        actionBarUtil.setup()

        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.attachment_list_coordinator_layout)
        ViewCompat.setOnApplyWindowInsetsListener(coordinatorLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }
            WindowInsetsCompat.CONSUMED
        }

        val extras = intent.extras
        if (extras == null) {
            finish()
            return
        }
        val itemType = extras.getString(ITEM_TYPE)?.let { forXmlNameOrNull(it) }
        val itemId = extras.getString(ITEM_ID)
        var vaultItem: VaultItem<*>? = null
        if (itemType != null && itemId != null) {
            vaultItem = vaultDataQuery.queryLegacy(VaultFilter(itemId, itemType))
        }
        if (vaultItem == null) {
            finish()
            return
        }

        val vaultItemLogAttachmentHelper =
            VaultItemLogAttachmentHelper(vaultItemLogger, vaultItem)
        val (attachments, isAttachmentListUpdated) = if (savedInstanceState != null) {
            savedInstanceState.getString(EXTRA_ATTACHMENTS_STRING) to savedInstanceState.getBoolean(
                EXTRA_HAVE_ATTACHMENTS_CHANGED
            )
        } else {
            intent.getStringExtra(ITEM_ATTACHMENTS) to false
        }
        attachmentListProvider = AttachmentListDataProvider(
                jsonAttachments = attachments,
                vaultItem = vaultItem,
                dataSaver = dataSaver,
                dataSync = dataSync,
                secureFileStorage = secureFileStorage
            )

        val openDocumentResultLauncher =
            registerForActivityResult(OpenDocumentResultContract()) { uri ->
                this.onOpenDocumentResult(
                    uri
                )
            }
        uploadAttachmentsPresenter =
            UploadAttachmentsPresenter(
                userFeaturesChecker = userFeaturesChecker,
                lockHelper = lockHelper,
                coroutineScope = this.lifecycleScope,
                sessionManager = mSessionManager,
                vaultItemLogAttachmentHelper = vaultItemLogAttachmentHelper,
                openDocumentResultLauncher = openDocumentResultLauncher
            ).apply {
                setProvider(uploadFileDataProvider)
                setView(UploadAttachmentsViewProxy(this@AttachmentListActivity))
            }

        val downloadPresenter = DownloadAttachmentsPresenter(
            this.lifecycleScope,
            vaultItemLogAttachmentHelper
        ).apply {
            setProvider(downloadFileDataProvider)
            setView(DownloadAttachmentsViewProxy(this@AttachmentListActivity))
        }

        attachmentListPresenter =
            AttachmentListPresenter(
                coroutineScope = this.lifecycleScope,
                uploadPresenter = uploadAttachmentsPresenter,
                downloadPresenter = downloadPresenter,
                deleteManager = deleteFileManager,
                lockHelper = lockHelper,
                vaultItemLogAttachmentHelper = vaultItemLogAttachmentHelper
            ).apply {
                this.isAttachmentListUpdated = isAttachmentListUpdated
                setProvider(attachmentListProvider)
                setView(AttachmentListViewProxy(this@AttachmentListActivity))
                onCreate()
            }

        
        announcementCenter.disable()
    }

    override fun onResume() {
        super.onResume()
        if (!applicationLocked) {
            attachmentListPresenter.onResume()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        
        outState.putString(
            EXTRA_ATTACHMENTS_STRING,
            Gson().toJson(getAttachmentsItems(attachmentListProvider))
        )
        outState.putBoolean(
            EXTRA_HAVE_ATTACHMENTS_CHANGED,
            attachmentListPresenter.isAttachmentListUpdated
        )
        super.onSaveInstanceState(outState)
    }

    private fun onOpenDocumentResult(uri: Uri?) {
        attachmentListPresenter.onOpenDocumentResult(uri)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            announcementCenter.restorePreviousState()
        }
    }

    private fun setAttachmentsResult(presenter: AttachmentListContract.Presenter) {
        val result = Intent()
        result.putExtra(EXTRA_HAVE_ATTACHMENTS_CHANGED, presenter.isAttachmentListUpdated)
        result.putExtra(
            EXTRA_ATTACHMENTS_STRING,
            Gson().toJson(getAttachmentsItems(attachmentListProvider))
        )
        setResult(RESULT_OK, result)
    }

    private fun getAttachmentsItems(provider: AttachmentListContract.DataProvider?) =
        provider?.attachments?.map { it.attachment } ?: emptyList()

    companion object {
        const val ITEM_ID = "itemId"
        const val ITEM_TYPE = "itemType"
        const val ITEM_ATTACHMENTS = "itemAttachments"
        const val REQUEST_CODE_ATTACHMENT_LIST = 123
        const val EXTRA_ATTACHMENTS_STRING = "attachments"
        const val EXTRA_HAVE_ATTACHMENTS_CHANGED = "haveAttachmentsChanged"
        const val LOG_TAG = "ATTACHMENTS"
    }
}