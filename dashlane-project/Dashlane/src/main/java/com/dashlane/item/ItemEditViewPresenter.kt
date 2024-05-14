package com.dashlane.item

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.dashlane.R
import com.dashlane.attachment.ui.AttachmentListActivity
import com.dashlane.authenticator.Otp
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierReplacedEvent
import com.dashlane.events.register
import com.dashlane.events.unregister
import com.dashlane.item.collection.CollectionSelectorActivity
import com.dashlane.item.linkedwebsites.LinkedServicesActivity
import com.dashlane.item.subview.ItemCollectionListSubView
import com.dashlane.item.subview.ItemCollectionListSubView.Collection
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.action.NewShareAction
import com.dashlane.item.subview.action.NewShareMenuAction
import com.dashlane.item.subview.action.ShareDetailsAction
import com.dashlane.item.subview.action.ShowAttachmentsMenuAction
import com.dashlane.item.subview.edit.ItemAuthenticatorEditSubView
import com.dashlane.item.subview.provider.credential.ItemScreenConfigurationAuthentifiantProvider
import com.dashlane.teamspaces.ui.TeamSpaceRestrictionNotificator
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.userfeatures.UserFeaturesChecker
import com.dashlane.util.DeviceUtils
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.showToaster
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.util.attachmentsAllowed
import com.dashlane.vault.util.hasAttachments
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
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
        registerForEvents(options, context)
    }

    private fun registerForEvents(
        options: ItemEditViewSetupOptions,
        context: Context
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

    override fun createMenu(menu: Menu, restrictionNotificator: TeamSpaceRestrictionNotificator): Boolean {
        val allMenus = mutableListOf<MenuAction>()
        
        provider.getScreenConfiguration().itemHeader?.menuActions?.let {
            allMenus.addAll(it)
        }

        
        val vaultItem = provider.vaultItem
        val summaryObject = vaultItem.toSummary<SummaryObject>()
        if (summaryObject.attachmentsAllowed(userFeaturesChecker)) {
            allMenus.add(ShowAttachmentsMenuAction(vaultItem))
        }
        
        if (vaultItem.syncObject !is SyncObject.SecureNote) {
            
            val canShare = !isNewItem(vaultItem) && !provider.isEditMode &&
                sharingPolicyDataProvider.canShareItem(vaultItem.toSummary()) && !summaryObject.hasAttachments()
            if (canShare) {
                allMenus.add(NewShareMenuAction(vaultItem, restrictionNotificator))
            }
            if (provider.isEditMode) {
                allMenus.add(
                    MenuAction(
                        R.string.dashlane_save,
                        R.drawable.save,
                        MenuItem.SHOW_AS_ACTION_ALWAYS
                    ) {
                        coroutineScope.launch(Dispatchers.Main) {
                            if (provider.save(context!!, provider.getScreenConfiguration().itemSubViews)) {
                                handleSuccess(vaultItem)
                            }
                        }
                    }
                )
            } else if (sharingPolicyDataProvider.canEditItem(vaultItem.toSummary(), isNewItem(vaultItem))) {
                allMenus.add(
                    MenuAction(
                        R.string.edit,
                        R.drawable.edit,
                        MenuItem.SHOW_AS_ACTION_ALWAYS
                    ) {
                        setupEditMode()
                    }
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

    override fun restorePasswordClicked() {
        coroutineScope.launch(Dispatchers.Main) {
            
            if (provider.restorePassword()) setup(context!!, currentOptions)
        }
    }

    override fun closeRestorePasswordClicked() {
        coroutineScope.launch(Dispatchers.Main) {
            
            provider.closeRestorePassword()
            setup(context!!, currentOptions)
        }
    }

    override fun otpRefreshed(otp: Otp) {
        coroutineScope.launch(Dispatchers.Main) {
            val subviews = provider.getScreenConfiguration().itemSubViews
            
            
            
            subviews.forEach {
                when (it) {
                    is ItemAuthenticatorEditSubView -> it.value = otp
                    is ItemSubViewWithActionWrapper -> {
                        if (it.itemSubView is ItemAuthenticatorEditSubView) {
                            it.itemSubView.value = otp
                        }
                    }
                }
            }
            provider.saveRefreshedOtp(otp)
        }
    }

    private fun addCollection(collection: Collection) {
        val subviews = provider.getScreenConfiguration().itemSubViews
        subviews.forEach { subview ->
            when (subview) {
                is ItemCollectionListSubView -> {
                    subview.value.value = (subview.value.value + collection).sortedBy { it.name }
                }
            }
        }
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
            NewShareAction.NEW_SHARE_REQUEST_CODE,
            ItemScreenConfigurationAuthentifiantProvider.GUIDED_PASSWORD_CHANGE_REQUEST_CODE -> {
                
                setup(context!!, currentOptions)
            }
            AttachmentListActivity.REQUEST_CODE_ATTACHMENT_LIST -> {
                handleAttachmentListResult(resultCode, data)
            }
            LinkedServicesActivity.SHOW_LINKED_SERVICES -> {
                handleLinkedServicesResult(data, resultCode)
            }
            CollectionSelectorActivity.SHOW_COLLECTION_SELECTOR -> {
                handleCollectionSelectorResult(data)
            }
            else -> view.listener.notifyPotentialBarCodeScan(requestCode, resultCode, data)
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

    private fun handleLinkedServicesResult(data: Intent?, resultCode: Int) {
        
        val linkedWebsites =
            data?.getStringArrayExtra(LinkedServicesActivity.RESULT_TEMPORARY_WEBSITES)?.toList()
        val linkedApps = data?.getStringArrayExtra(LinkedServicesActivity.RESULT_TEMPORARY_APPS)?.toList()
        if (linkedWebsites != null || linkedApps != null) {
            provider.setTemporaryLinkedServices(context!!, view.listener, linkedWebsites, linkedApps)
            coroutineScope.launch(Dispatchers.Main) {
                setupJob?.join()
                refreshUi(toolbarCollapsed = currentOptions.toolbarCollapsed)
            }
        }

        if (resultCode == LinkedServicesActivity.RESULT_DATA_SAVED) {
            
            view.showSaveConfirmation()

            coroutineScope.launch(Dispatchers.Main) {
                setupJob?.join()
                
                setup(context!!, currentOptions.copy(forceEdit = provider.isEditMode))
            }
        }
    }

    private fun handleCollectionSelectorResult(data: Intent?) {
        coroutineScope.launch(Dispatchers.Main) {
            setupJob?.join()
            val selectedCollection =
                data?.getParcelableExtraCompat(
                    CollectionSelectorActivity.RESULT_TEMPORARY_COLLECTION,
                    Collection::class.java
                )
            selectedCollection?.let { addCollection(it) }
            if (!provider.isEditMode) {
                provider.save(context!!, provider.getScreenConfiguration().itemSubViews)
            }
        }
    }

    override fun onNewIntent(data: Intent) {
        provider.onNewIntent(data, coroutineScope)
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
        
        context?.let { provider.setTemporaryLinkedServices(it, view.listener, null, null) }

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