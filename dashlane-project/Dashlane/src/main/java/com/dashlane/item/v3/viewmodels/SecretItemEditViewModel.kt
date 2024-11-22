package com.dashlane.item.v3.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.dashlane.events.AppEvents
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.v3.builders.SecretBuilder
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.loaders.SecretAsyncDataLoader
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
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.createSecret
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.update

@HiltViewModel
class SecretItemEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dataSync: DataSync,
    genericDataQuery: GenericDataQuery,
    vaultDataQuery: VaultDataQuery,
    appEvents: AppEvents,
    menuActionHelper: MenuActionHelper,
    secretFormBuilder: SecretBuilder,
    vaultItemCopy: VaultItemCopyService,
    sensitiveFieldLoader: SensitiveFieldLoader,
    frozenStateManager: FrozenStateManager,
    itemEditRepository: ItemEditRepository,
    collectionsRepository: CollectionsRepository,
    sharingPolicyDataProvider: SharingPolicyDataProvider,
    navigator: Navigator,
    secretAsyncDataLoader: SecretAsyncDataLoader,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
) : ItemEditViewModel<SecretFormData>(
    savedStateHandle = savedStateHandle,
    dataSync = dataSync,
    genericDataQuery = genericDataQuery,
    vaultDataQuery = vaultDataQuery,
    sensitiveFieldLoader = sensitiveFieldLoader,
    vaultItemCopy = vaultItemCopy,
    itemEditRepository = itemEditRepository,
    appEvents = appEvents,
    menuActionHelper = menuActionHelper,
    formDataBuilder = secretFormBuilder,
    asyncDataLoader = secretAsyncDataLoader,
    frozenStateManager = frozenStateManager,
    collectionsRepository = collectionsRepository,
    sharingPolicyDataProvider = sharingPolicyDataProvider,
    navigator = navigator,
) {
    override val mutableUiState: MutableViewStateFlow<ItemEditState<SecretFormData>, ItemEditSideEffect> =
        MutableViewStateFlow(
            ItemEditState(
                itemId = navArgs.uid ?: "",
                isNew = navArgs.uid == null,
                isEditMode = navArgs.forceEdit
            )
        )

    override fun createNewItem() = createSecret(
        spaceId = teamSpaceAccessorProvider.get()?.currentBusinessTeam?.teamId,
    )

    fun onSecuredChanged(secured: Boolean) {
        mutableUiState.update {
            it.copyCurrentFormData {
                it.copy(secured = secured)
            }
        }
    }

    fun onTitleChanged(title: String) {
        mutableUiState.update {
            it.copyCurrentCommonData {
                it.copy(
                    name = title,
                )
            }
        }
    }

    fun onContentChanged(content: String) {
        mutableUiState.update {
            it.copyCurrentFormData {
                it.copy(
                    content = content,
                )
            }
        }
    }

    fun copySecretContent() {
        copyToClipboard(CopyField.SecretValue)
    }
}