package com.dashlane.item.v3.viewmodels

import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import androidx.lifecycle.SavedStateHandle
import com.dashlane.events.AppEvents
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.activityLog
import com.dashlane.item.v3.builders.SecureNoteBuilder
import com.dashlane.item.v3.data.SecureNoteContentFeedback
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.loaders.SecureNoteAsyncDataLoader
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.util.MenuActionHelper
import com.dashlane.navigation.Navigator
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.Properties
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.sync.DataSync
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.model.createSecureNote
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SecureNoteItemEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dataSync: DataSync,
    genericDataQuery: GenericDataQuery,
    vaultDataQuery: VaultDataQuery,
    appEvents: AppEvents,
    menuActionHelper: MenuActionHelper,
    secureNoteFormBuilder: SecureNoteBuilder,
    vaultItemCopy: VaultItemCopyService,
    frozenStateManager: FrozenStateManager,
    itemEditRepository: ItemEditRepository,
    sharingPolicyDataProvider: SharingPolicyDataProvider,
    navigator: Navigator,
    secureNoteAsyncDataLoader: SecureNoteAsyncDataLoader,
    private val clipboardManager: ClipboardManager?,
    private val logRepository: LogRepository,
) : ItemEditViewModel<SecureNoteFormData>(
        savedStateHandle = savedStateHandle,
        dataSync = dataSync,
        genericDataQuery = genericDataQuery,
        vaultDataQuery = vaultDataQuery,
        vaultItemCopy = vaultItemCopy,
        itemEditRepository = itemEditRepository,
        appEvents = appEvents,
        menuActionHelper = menuActionHelper,
        formDataBuilder = secureNoteFormBuilder,
        asyncDataLoader = secureNoteAsyncDataLoader,
        frozenStateManager = frozenStateManager,
        sharingPolicyDataProvider = sharingPolicyDataProvider,
        navigator = navigator,
    ),
    OnPrimaryClipChangedListener {
    override fun createNewItem() = createSecureNote()

    override fun onPrimaryClipChanged() {
        val clip = clipboardManager?.primaryClip?.getItemAt(0)?.text
        val datas = mutableUiState.viewState.value.datas
        if (clip != null && datas?.current?.formData?.content?.contains(clip) == true) {
            logRepository.queueEvent(
                activityLog(
                    logType = ActivityLog.LogType.USER_COPIED_SECURE_NOTE_FIELD,
                    properties = Properties(name = datas.current.commonData.name),
                ),
            )
        }
    }

    @Suppress(
        
        "kotlin:S6313",
    )
    override suspend fun goToEditMode(state: ItemEditState<SecureNoteFormData>): ItemEditState<SecureNoteFormData> {
        if (state.datas?.current?.formData?.secured == true) {
            clipboardManager?.addPrimaryClipChangedListener(this)
        }
        return super.goToEditMode(state)
    }

    override fun goToViewMode(state: ItemEditState<SecureNoteFormData>): ItemEditState<SecureNoteFormData> {
        clipboardManager?.removePrimaryClipChangedListener(this)
        return super.goToViewMode(state)
    }

    override fun onCleared() {
        clipboardManager?.removePrimaryClipChangedListener(this)
        super.onCleared()
    }

    fun onSecuredChanged(secured: Boolean) {
        mutableUiState.update {
            it.copyCurrentFormData {
                it.copy(secured = secured)
            }
        }
    }

    fun updateSecureNoteType(type: SyncObject.SecureNoteType) {
        mutableUiState.update {
            it.copyCurrentFormData {
                it.copy(secureNoteType = type)
            }
        }
    }

    fun onSecureNoteContentChanged(content: String) {
        mutableUiState.update {
            it.copyCurrentFormData {
                it.copy(
                    content = content,
                    contentFeedback = SecureNoteContentFeedback.fromContent(content),
                )
            }
        }
    }

    override fun hasError(current: Data<SecureNoteFormData>): Boolean {
        return super.hasError(current) || current.formData.contentFeedback?.error ?: false
    }

    companion object {
        const val SECURE_NOTE_CONTENT_SIZE_CHARACTER_LIMIT = 10_000
    }
}