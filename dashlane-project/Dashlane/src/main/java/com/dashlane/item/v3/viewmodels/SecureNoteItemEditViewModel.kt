package com.dashlane.item.v3.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.dashlane.events.AppEvents
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.v3.builders.SecureNoteBuilder
import com.dashlane.item.v3.data.SecureNoteContentFeedback
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.loaders.SecureNoteAsyncDataLoader
import com.dashlane.item.v3.repositories.CollectionsRepository
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.util.MenuActionHelper
import com.dashlane.item.v3.util.SensitiveFieldLoader
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.navigation.Navigator
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.sync.DataSync
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.model.createSecureNote
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.update

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
    sensitiveFieldLoader: SensitiveFieldLoader,
    frozenStateManager: FrozenStateManager,
    itemEditRepository: ItemEditRepository,
    collectionsRepository: CollectionsRepository,
    sharingPolicyDataProvider: SharingPolicyDataProvider,
    navigator: Navigator,
    secureNoteAsyncDataLoader: SecureNoteAsyncDataLoader
) : ItemEditViewModel<SecureNoteFormData>(
    savedStateHandle = savedStateHandle,
    dataSync = dataSync,
    genericDataQuery = genericDataQuery,
    vaultDataQuery = vaultDataQuery,
    sensitiveFieldLoader = sensitiveFieldLoader,
    vaultItemCopy = vaultItemCopy,
    itemEditRepository = itemEditRepository,
    appEvents = appEvents,
    menuActionHelper = menuActionHelper,
    formDataBuilder = secureNoteFormBuilder,
    asyncDataLoader = secureNoteAsyncDataLoader,
    frozenStateManager = frozenStateManager,
    collectionsRepository = collectionsRepository,
    sharingPolicyDataProvider = sharingPolicyDataProvider,
    navigator = navigator,
) {
    
    override val mutableUiState: MutableViewStateFlow<ItemEditState<SecureNoteFormData>, ItemEditSideEffect> =
        MutableViewStateFlow(
            ItemEditState(
                itemId = navArgs.uid ?: "",
                isNew = navArgs.uid == null,
                isEditMode = navArgs.forceEdit
            )
        )

    override fun createNewItem() = createSecureNote()

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
                    contentFeedback = SecureNoteContentFeedback.fromContent(content)
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