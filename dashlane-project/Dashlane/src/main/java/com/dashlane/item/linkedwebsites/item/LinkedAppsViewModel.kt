package com.dashlane.item.linkedwebsites.item

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dashlane.R
import com.dashlane.item.linkedwebsites.LinkedServicesFragmentArgs
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.url.toUrlOrNull
import com.dashlane.util.getAppIcon
import com.dashlane.util.getAppName
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getAllLinkedPackageName
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class LinkedAppsViewModel @Inject constructor(
    private val packageManager: PackageManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val state: StateFlow<LinkedAppsUIState>
        get() = _state.asStateFlow()
    private val _state: MutableStateFlow<LinkedAppsUIState> = MutableStateFlow(
        LinkedAppsUIState(
            viewProvider = listOf(),
            showEmptyState = false,
            actionOpenApp = null,
            actionOpenStore = null,
            actionOpenWebsite = null
        )
    )
    private var vaultItem: VaultItem<SyncObject.Authentifiant>? = null
    private var isEditMode = false
    private val addedByYouApps = mutableListOf<String>()
    private val args = LinkedServicesFragmentArgs.fromSavedStateHandle(savedStateHandle)

    fun setupVaultItem(vaultItem: VaultItem<SyncObject.Authentifiant>) {
        this.vaultItem = vaultItem
        buildApps()
    }

    fun changeEditMode(editMode: Boolean) {
        if (isEditMode == editMode) return
        isEditMode = editMode
        buildApps()
    }

    private fun buildApps() {
        val items = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        val linkedAndroidApps = vaultItem?.syncObject?.linkedServices?.associatedAndroidApps
        addedByYouApps.clear()
        items.addAll(buildMainWebsite())
        val addedByUser = buildLinkedByUserApps(linkedAndroidApps)
        val addedByDashlane = buildLinkedByDashlaneApps(linkedAndroidApps)
        items.addAll(addedByUser)
        items.addAll(addedByDashlane)
        _state.update {
            it.copy(viewProvider = items, showEmptyState = (addedByUser + addedByDashlane).isEmpty())
        }
    }

    private fun buildMainWebsite(): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        return args.url?.let {
            listOf(
                LinkedServicesHeaderItem(R.string.multi_domain_credentials_main_website, false),
                LinkedWebsitesItem(
                    defaultUrl = it,
                    isMain = true,
                    isEditable = false,
                    isPageEditMode = isEditMode,
                    openWebsiteListener = this::openMainWebsite
                )
            )
        } ?: emptyList()
    }

    private fun buildLinkedByUserApps(linkedAndroidApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>?): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        val items = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        val linkedByUser = linkedAndroidApps
            ?.filter { it.linkSource == SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource.USER }
            ?.filter { args.temporaryApps?.contains(it.packageName) ?: true }
        if (!linkedByUser.isNullOrEmpty()) {
            items.add(LinkedServicesHeaderItem(R.string.multi_domain_credentials_added_by_you, false))
        }
        linkedByUser?.mapNotNull { it.packageName }?.let { addedByYouApps.addAll(it) }
        linkedByUser?.forEach {
            var appName: String? = null
            var appDrawable: Drawable? = null
            it.packageName?.let { packageName ->
                appName = packageManager.getAppName(packageName)
                appDrawable = packageManager.getAppIcon(packageName)
            }
            items.add(
                LinkedAppsItem(
                    appName = appName ?: it.name,
                    appIcon = appDrawable,
                    packageName = it.packageName ?: "",
                    removeAppListener = ::removeWebsite,
                    openAppListener = ::openApp,
                    isEditable = true,
                    isAppInstalled = appName != null && appDrawable != null,
                    isPageEditMode = isEditMode
                )
            )
        }
        return items
    }

    private fun buildLinkedByDashlaneApps(linkedAndroidApps: List<SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps>?): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        val linkedByDashlane =
            linkedAndroidApps?.filter { it.linkSource == SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource.DASHLANE }
        val items = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        if (!linkedByDashlane.isNullOrEmpty()) {
            items.add(LinkedServicesHeaderItem(R.string.multi_domain_credentials_automatically_added, true))
        }
        linkedByDashlane?.map {
            var appName: String? = null
            var appDrawable: Drawable? = null
            it.packageName?.let { packageName ->
                appName = packageManager.getAppName(packageName)
                appDrawable = packageManager.getAppIcon(packageName)
            }
            LinkedAppsItem(
                appName = appName ?: it.name,
                appIcon = appDrawable,
                packageName = it.packageName ?: "",
                removeAppListener = ::removeWebsite,
                openAppListener = ::openApp,
                isEditable = false,
                isAppInstalled = appName != null && appDrawable != null,
                isPageEditMode = isEditMode
            )
        }?.sortedByDescending { it.isAppInstalled }?.let { items.addAll(it) }
        return items
    }

    private fun openApp(item: LinkedAppsItem) {
        if (item.isAppInstalled) {
            _state.update { it.copy(actionOpenApp = item.packageName) }
        } else {
            _state.update { it.copy(actionOpenStore = item.packageName) }
        }
    }

    private fun removeWebsite(item: LinkedAppsItem) {
        addedByYouApps.remove(item.packageName)
        _state.update {
            it.copy(viewProvider = it.viewProvider - item)
        }
    }

    fun getEditableAppsResult() = addedByYouApps.toList()

    fun onAppOpened() {
        _state.update { it.copy(actionOpenApp = null, actionOpenStore = null) }
    }

    private fun openMainWebsite(item: LinkedWebsitesItem) {
        val packageNames = vaultItem?.toSummary<SummaryObject.Authentifiant>()?.linkedServices.getAllLinkedPackageName()
        _state.update {
            it.copy(
                actionOpenWebsite = LinkedWebsitesViewModel.LinkedWebsitesUIState.LinkedWebsitesUIStateLoginOpener(
                    item.defaultUrl.toUrlOrNull()?.toString(),
                    packageNames
                )
            )
        }
    }

    fun websiteOpened() {
        _state.update {
            it.copy(actionOpenWebsite = null)
        }
    }

    data class LinkedAppsUIState(
        val viewProvider: List<DashlaneRecyclerAdapter.ViewTypeProvider>,
        val showEmptyState: Boolean,
        val actionOpenApp: String?,
        val actionOpenStore: String?,
        val actionOpenWebsite: LinkedWebsitesViewModel.LinkedWebsitesUIState.LinkedWebsitesUIStateLoginOpener?
    )
}