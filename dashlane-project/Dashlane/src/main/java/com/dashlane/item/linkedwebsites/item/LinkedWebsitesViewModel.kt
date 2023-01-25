package com.dashlane.item.linkedwebsites.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dashlane.R
import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.item.linkedwebsites.LinkedServicesActivity
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.url.toUrlOrNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getAllLinkedPackageName
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LinkedWebsitesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state: StateFlow<LinkedWebsitesUIState>
        get() = _state
    private val _state: MutableStateFlow<LinkedWebsitesUIState> = MutableStateFlow(
        LinkedWebsitesUIState(viewProvider = listOf(), null)
    )
    private val addNew = savedStateHandle.get<Boolean>(LinkedServicesActivity.PARAM_ADD_NEW)!!
    private val urlDomain = savedStateHandle.get<String>(LinkedServicesActivity.PARAM_URL_DOMAIN)
    private val temporaryLinkedWebsites =
        savedStateHandle.get<Array<String>>(LinkedServicesActivity.PARAM_TEMPORARY_WEBSITES)!!.toList()
    private var vaultItem: VaultItem<SyncObject.Authentifiant>? = null
    private val editableResult = mutableMapOf<String, String>()
    private var isEditMode = false

    fun setupVaultItem(vaultItem: VaultItem<SyncObject.Authentifiant>) {
        this.vaultItem = vaultItem
        buildWebsites()
    }

    fun changeEditMode(editMode: Boolean) {
        if (isEditMode == editMode) return
        isEditMode = editMode
        buildWebsites()
    }

    private fun buildWebsites() {
        val viewList = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        viewList.addAll(buildMainWebsite())
        viewList.addAll(buildAddedByYouWebsites())
        viewList.addAll(buildAddedByDashlaneWebsites())
        _state.update {
            it.copy(viewProvider = viewList)
        }
    }

    private fun buildMainWebsite(): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        return urlDomain?.let {
            listOf(
                LinkedServicesHeaderItem(R.string.multi_domain_credentials_main_website, false), LinkedWebsitesItem(
                    urlDomain,
                    isMain = true,
                    isEditable = false,
                    isPageEditMode = isEditMode,
                    onValueUpdated = this::onWebsiteValueUpdated,
                    getUrlValue = this::getUrlValue,
                    removeWebsiteListener = this::removeWebsite,
                    openWebsiteListener = this::openWebsite
                )
            )
        } ?: emptyList()
    }

    private fun buildAddedByYouWebsites(): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        val items = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        if (temporaryLinkedWebsites.isNotEmpty() || isEditMode) {
            items.add(LinkedServicesHeaderItem(R.string.multi_domain_credentials_added_by_you, false))
        }
        temporaryLinkedWebsites.forEach {
            items.add(LinkedWebsitesItem(
                it,
                isEditable = isEditMode,
                isPageEditMode = isEditMode,
                onValueUpdated = this::onWebsiteValueUpdated,
                getUrlValue = this::getUrlValue,
                removeWebsiteListener = this::removeWebsite,
                openWebsiteListener = this::openWebsite
            ).apply {
                editableResult[uid] = it
            })
        }
        if (isEditMode) {
            if (addNew) {
                items.add(
                    LinkedWebsitesItem(
                        "",
                        isEditable = true,
                        isPageEditMode = isEditMode,
                        requestFocus = true,
                        onValueUpdated = this::onWebsiteValueUpdated,
                        getUrlValue = this::getUrlValue,
                        removeWebsiteListener = this::removeWebsite,
                        openWebsiteListener = this::openWebsite
                    )
                )
            }
            items.add(LinkedWebsitesAddItem(R.string.multi_domain_credentials_add_website, ::addAnotherWebsite))
        }
        return items
    }

    private fun buildAddedByDashlaneWebsites(): List<DashlaneRecyclerAdapter.ViewTypeProvider> {
        val items = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>()
        KnownLinkedDomains.getMatchingLinkedDomainSet(urlDomain)?.also {
            if (it.isNotEmpty()) {
                items.add(LinkedServicesHeaderItem(R.string.multi_domain_credentials_automatically_added, true))
            }
        }?.forEach {
            items.add(
                LinkedWebsitesItem(
                    it.value,
                    isEditable = false,
                    isPageEditMode = isEditMode,
                    onValueUpdated = this::onWebsiteValueUpdated,
                    getUrlValue = this::getUrlValue,
                    removeWebsiteListener = this::removeWebsite,
                    openWebsiteListener = this::openWebsite
                )
            )
        }
        return items
    }

    private fun addAnotherWebsite(position: Int) {
        resetFocus()
        val viewProviderWithNewOne = mutableListOf<DashlaneRecyclerAdapter.ViewTypeProvider>().also {
            it.addAll(_state.value.viewProvider)
            it.add(
                position, LinkedWebsitesItem(
                    "",
                    isEditable = true,
                    isPageEditMode = isEditMode,
                    requestFocus = true,
                    onValueUpdated = this::onWebsiteValueUpdated,
                    getUrlValue = this::getUrlValue,
                    removeWebsiteListener = this::removeWebsite,
                    openWebsiteListener = this::openWebsite
                )
            )
        }
        _state.update {
            it.copy(viewProvider = viewProviderWithNewOne)
        }
    }

    private fun removeWebsite(item: LinkedWebsitesItem) {
        resetFocus()
        _state.update {
            it.copy(viewProvider = it.viewProvider - item)
        }
    }

    private fun resetFocus() {
        _state.value.viewProvider.filterIsInstance(LinkedWebsitesItem::class.java).forEach {
            it.requestFocus = false
        }
    }

    private fun onWebsiteValueUpdated(uid: String, value: String) {
        editableResult[uid] = value
    }

    private fun getUrlValue(uid: String, defaultUrl: String): String {
        return editableResult[uid] ?: defaultUrl
    }

    

    fun getMutableWebsitesValue() = editableResult.filter { result ->
        _state.value.viewProvider.filterIsInstance(LinkedWebsitesItem::class.java).any { it.uid == result.key }
    }.values.filter { it.isNotBlank() }

    

    private fun openWebsite(item: LinkedWebsitesItem) {
        val packageNames = if (item.isMain) {
            vaultItem?.toSummary<SummaryObject.Authentifiant>()?.linkedServices.getAllLinkedPackageName()
        } else {
            setOf()
        }
        _state.update {
            it.copy(
                actionOpenWebsite = LinkedWebsitesUIState.LinkedWebsitesUIStateLoginOpener(
                    item.defaultUrl.toUrlOrNull()?.toString(), packageNames
                )
            )
        }
    }

    fun websiteOpened() {
        _state.update {
            it.copy(actionOpenWebsite = null)
        }
    }

    data class LinkedWebsitesUIState(
        val viewProvider: List<DashlaneRecyclerAdapter.ViewTypeProvider>,
        val actionOpenWebsite: LinkedWebsitesUIStateLoginOpener?
    ) {
        data class LinkedWebsitesUIStateLoginOpener(val url: String?, val packageNames: Set<String>)
    }
}