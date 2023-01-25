package com.dashlane.item.linkedwebsites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkedServicesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataProvider: LinkedServicesDataProvider,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    private val userFeaturesChecker: UserFeaturesChecker,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel(), LinkedServicesContract.ViewModel {

    val fromViewOnly = savedStateHandle.get<Boolean>(LinkedServicesActivity.PARAM_FROM_VIEW_ONLY)!!
    val state: StateFlow<LinkedServicesUIState>
        get() = _state
    private val _state: MutableStateFlow<LinkedServicesUIState> = MutableStateFlow(
        LinkedServicesUIState(
            vaultItem = null,
            editMode = !fromViewOnly,
            actionClosePageAfterSave = false,
            closePageImmediate = false
        )
    )
    private var vaultItem: VaultItem<SyncObject.Authentifiant>? = null
    var isEditMode = !fromViewOnly
        private set
        get() = _state.value.editMode
    private val temporaryLinkedWebsites =
        savedStateHandle.get<Array<String>>(LinkedServicesActivity.PARAM_TEMPORARY_WEBSITES)!!.toList()
    private val temporaryLinkedApps =
        savedStateHandle.get<Array<String>>(LinkedServicesActivity.PARAM_TEMPORARY_APPS)?.toList()
    private val itemId = savedStateHandle.get<String>(LinkedServicesActivity.PARAM_ITEM_ID) ?: ""

    init {
        
        if (itemId.isEmpty()) {
            _state.update {
                it.copy(closePageImmediate = true)
            }
        }
        viewModelScope.launch {
            vaultItem = dataProvider.getItem(itemId)
            _state.update {
                it.copy(vaultItem = vaultItem)
            }
        }
    }

    override fun save(linkedWebsites: List<String>, linkedApps: List<String>) {
        viewModelScope.launch(ioDispatcher) {
            vaultItem?.let {
                dataProvider.save(it, linkedWebsites, linkedApps)
            }
            _state.update {
                it.copy(actionClosePageAfterSave = true)
            }
        }
    }

    override fun switchEditMode() {
        _state.update {
            it.copy(editMode = !isEditMode)
        }
    }

    override fun hasOtherItemsDuplicate(linkedWebsites: List<String>): Pair<String, String>? =
        dataProvider.getDuplicateWebsitesItem(vaultItem, linkedWebsites)

    override fun hasWebsitesToSave(linkedWebsites: List<String>): Boolean =
        temporaryLinkedWebsites != linkedWebsites

    override fun hasAppsToSave(linkedApps: List<String>): Boolean =
        (temporaryLinkedApps
            ?: vaultItem?.syncObject?.linkedServices?.associatedAndroidApps
                ?.filter { it.linkSource == SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource.USER }
                ?.map { it.packageName } ?: listOf()) != linkedApps

    override fun canEdit(): Boolean =
        vaultItem?.let { sharingPolicyDataProvider.canEditItem(it.toSummary(), false) } ?: false

    override fun doneButtonActivated(): Boolean =
        userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.LINKED_WEBSITES_DONE_BUTTON)
}