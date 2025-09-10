package com.dashlane.item

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dashlane.R
import com.dashlane.attachment.ui.AttachmentListActivity
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierReplacedEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.featureflipping.FeatureFlip.ATTACHMENT_ALL_ITEMS
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.action.NewShareAction
import com.dashlane.item.subview.action.NewShareMenuAction
import com.dashlane.item.subview.action.ShareDetailsAction
import com.dashlane.item.subview.action.ShowAttachmentsMenuAction
import com.dashlane.item.subview.action.showAttachments
import com.dashlane.securefile.extensions.hasAttachments
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.util.DeviceUtils
import com.dashlane.util.showToaster
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.utils.attachmentsAllowed
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("LargeClass")
class ItemEditViewPresenter :
    BasePresenter<ItemEditViewContract.DataProvider, ItemEditViewContract.View>(),
    ItemEditViewContract.Presenter {
    override val isSecureNote: Boolean
        get() = tryOrNull { provider.vaultItem.syncObject is SyncObject.SecureNote } ?: false

    lateinit var sharingPolicyDataProvider: SharingPolicyDataProvider
    lateinit var userFeaturesChecker: UserFeaturesChecker
    lateinit var coroutineScope: CoroutineScope
    lateinit var appEvents: AppEvents
    lateinit var frozenStateManager: FrozenStateManager

    private var setupJob: Job? = null

    private lateinit var currentOptions: ItemEditViewSetupOptions

    override fun setup(context: Context, options: ItemEditViewSetupOptions) {
        currentOptions = options
        if (setupJob != null) {
            appEvents.unregister<DataIdentifierReplacedEvent>(this)
        }
        setupJob = coroutineScope.launch(Dispatchers.Main) {
            val itemEditActivity = activity ?: return@launch
            if (itemEditActivity.isFinishing || itemEditActivity.isDestroyed) return@launch
            if (provider.setup(context, options, view.listener)) {
                refreshUi(options.toolbarCollapsed)
                provider.onSetupEnd(context, options, view.listener)
            } else {
                
                itemEditActivity.apply {
                    finish()
                    showToaster(
                        R.string.error_type_or_uid_invalid_on_item_opening,
                        Toast.LENGTH_SHORT,
                    )
                }
            }
        }
        registerForEvents(options, context)
    }

    private fun registerForEvents(
        options: ItemEditViewSetupOptions,
        context: Context,
    ) {
        appEvents.register<DataIdentifierReplacedEvent>(this) { event ->
            if (event.oldItemId == options.uid) {
                coroutineScope.launch {
                    
                    
                    setupJob?.join()
                    
                    
                    
                    val animTime =
                        context.resources.getInteger(android.R.integer.config_shortAnimTime)
                            .toLong()
                    delay(animTime)
                    
                    currentOptions = options.copy(uid = event.newItemId)
                    setup(context, currentOptions)
                }
            }
        }
    }

    @Suppress("LongMethod")
    override fun createMenu(
        menu: Menu,
        teamspaceRestrictionNotificator: TeamSpaceRestrictionNotificator,
        openItemHistory: (SyncObject.Authentifiant) -> Unit,
    ): Boolean {
        val allMenus = mutableListOf<MenuAction>()
        val isAccountFrozen = frozenStateManager.isAccountFrozen
        
        provider.getScreenConfiguration().itemHeader?.menuActions?.let {
            allMenus.addAll(it)
        }

        
        val vaultItem = provider.vaultItem
        val summaryObject = vaultItem.toSummary<SummaryObject>()
        if (summaryObject.attachmentsAllowed(
                attachmentAllItems = userFeaturesChecker.has(ATTACHMENT_ALL_ITEMS),
                isAccountFrozen = isAccountFrozen,
                
                
                
                hasCollections = false,
            )
        ) {
            allMenus.add(
                ShowAttachmentsMenuAction(
                    action = { activity ->
                        summaryObject.showAttachments(activity)
                    },
                ),
            )
        }
        
        if (vaultItem.syncObject !is SyncObject.SecureNote) {
            
            val canShare = !isNewItem(vaultItem) && !provider.isEditMode &&
                sharingPolicyDataProvider.canShareItem(vaultItem.toSummary()) && !summaryObject.hasAttachments() && !isAccountFrozen
            if (canShare) {
                allMenus.add(NewShareMenuAction(summaryObject, teamspaceRestrictionNotificator))
            }
            if (provider.isEditMode) {
                allMenus.add(
                    MenuAction(
                        R.string.dashlane_save,
                        R.drawable.save,
                        MenuItem.SHOW_AS_ACTION_ALWAYS,
                    ) {
                        coroutineScope.launch(Dispatchers.Main) {
                            if (provider.save(context!!, provider.getScreenConfiguration().itemSubViews)) {
                                handleSuccess(vaultItem)
                            }
                        }
                    },
                )
            } else if (sharingPolicyDataProvider.canEditItem(
                    vaultItem.toSummary(),
                    isNewItem(vaultItem),
                    isAccountFrozen,
                )
            ) {
                allMenus.add(
                    MenuAction(
                        R.string.edit,
                        R.drawable.edit,
                        MenuItem.SHOW_AS_ACTION_ALWAYS,
                    ) {
                        setupEditMode()
                    },
                )
            }
        }
        view.setMenus(allMenus, menu)
        return true
    }

    override fun selectMenuItem(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return view.selectMenuItem(item)
    }

    override fun deleteClicked() {
        view.showConfirmDeleteDialog(provider.vaultItem.uid, provider.vaultItem.isShared())
    }

    override fun onBackPressed() {
        DeviceUtils.hideKeyboard(activity)
        askForSaveOrExit()
    }

    override fun onNewActivityLaunching(callback: ItemEditViewContract.Presenter.Callback) {
        
        if (provider.isSetup && provider.isEditMode && provider.vaultItem.syncObjectType == SyncObjectType.SECURE_NOTE) {
            coroutineScope.launch(Dispatchers.Main) {
                provider.save(context!!, provider.getScreenConfiguration().itemSubViews)
                currentOptions = currentOptions.copy(uid = provider.vaultItem.uid)
                callback.onCompletion()
            }
        } else {
            callback.onCompletion()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ShareDetailsAction.SHOW_SHARING_DETAILS_REQUEST_CODE -> {
                handleSharingDetailsResult(data)
            }
            NewShareAction.NEW_SHARE_REQUEST_CODE -> {
                
                setup(context!!, currentOptions)
            }
            AttachmentListActivity.REQUEST_CODE_ATTACHMENT_LIST -> {
                handleAttachmentListResult(resultCode, data)
            }
        }
    }

    private fun handleAttachmentListResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        val hasAttachmentChanged =
            data.getBooleanExtra(AttachmentListActivity.EXTRA_HAVE_ATTACHMENTS_CHANGED, false)
        if (hasAttachmentChanged) {
            
            val newAttachments = data.getStringExtra(AttachmentListActivity.EXTRA_ATTACHMENTS_STRING)
            coroutineScope.launch(Dispatchers.Main) {
                
                
                setupJob?.join()
                provider.save(context!!, provider.getScreenConfiguration().itemSubViews, newAttachments)
                
                setup(context!!, currentOptions)
            }
        }
    }

    private fun handleSharingDetailsResult(data: Intent?) {
        if (data == null || !data.getBooleanExtra(ShareDetailsAction.EXTRA_UID_CHANGED, false)) {
            
            setup(context!!, currentOptions)
        } else {
            
            activity!!.finish()
        }
    }

    override fun isToolbarCollapsed(): Boolean {
        return view.isToolbarCollapsed
    }

    private fun refreshUi(toolbarCollapsed: Boolean, isChangingMode: Boolean = false) {
        view.setConfiguration(provider.getScreenConfiguration(), provider.isEditMode, toolbarCollapsed, isChangingMode)
    }

    private fun setupViewMode(item: VaultItem<*>) {
        if (item.syncObject is SyncObject.SecureNote) {
            
            activity!!.finish()
            return
        }
        val vaultItem = provider.vaultItem
        
        currentOptions = currentOptions.copy(uid = vaultItem.uid, forceEdit = false)
        provider.changeMode(context!!, false, view.listener)
        if (item.syncState == SyncState.DELETED) {
            activity!!.finish()
        } else {
            refreshUi(toolbarCollapsed = false, isChangingMode = true)
        }
    }

    private fun setupEditMode() {
        provider.changeMode(context!!, true, view.listener)
        refreshUi(toolbarCollapsed = true, isChangingMode = true)
    }

    override fun onStart() {
        provider.logViewDisplay()
    }

    override fun onPause() {
        if (provider.isSetup) {
            val item = provider.vaultItem
            val shouldSave = item.syncObject is SyncObject.SecureNote &&
                item.syncState != SyncState.DELETED &&
                (provider.hasUnsavedChanges() || isNewItem(item))
            if (!shouldSave) return
            
            coroutineScope.launch(Dispatchers.Default) {
                provider.save(context!!, provider.getScreenConfiguration().itemSubViews)
            }
        }
    }

    private fun askForSaveOrExit() {
        if (!provider.isSetup || !provider.isEditMode) {
            activity!!.finish()
            return
        }
        val item = provider.vaultItem
        if (provider.hasUnsavedChanges()) {
            if (item.syncObject is SyncObject.SecureNote) {
                
                coroutineScope.launch(Dispatchers.Main) {
                    if (provider.save(context!!, provider.getScreenConfiguration().itemSubViews)) {
                        setupViewMode(item)
                    }
                }
                return
            }
            view.askForSave { saveChanges ->
                if (!saveChanges) {
                    
                    abortChanges(item)
                } else {
                    
                    coroutineScope.launch(Dispatchers.Main) {
                        if (provider.save(context!!, provider.getScreenConfiguration().itemSubViews)) {
                            handleSuccess(item)
                        }
                    }
                }
            }
        } else {
            abortChanges(item)
        }
    }

    private fun abortChanges(item: VaultItem<*>) {
        if (isNewItem(item)) {
            
            activity!!.finish()
            return
        }

        setupViewMode(item)
    }

    private fun isNewItem(vaultItem: VaultItem<*>): Boolean {
        return !vaultItem.hasBeenSaved
    }

    private fun handleSuccess(vaultItem: VaultItem<*>) {
        view.showSaveConfirmation()
        DeviceUtils.hideKeyboard(activity)

        val intent = currentOptions.successIntent
        if (intent == null) {
            setupViewMode(vaultItem)
        } else {
            activity?.run {
                setResult(Activity.RESULT_OK)
                finish()
                startActivity(intent)
            }
        }
    }
}