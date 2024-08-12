package com.dashlane.item.v3.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierReplacedEvent
import com.dashlane.events.registerAsFlow
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.v3.ItemEditFragmentArgs
import com.dashlane.item.v3.builders.SpecializedFormBuilder
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.InfoBoxData
import com.dashlane.item.v3.data.ItemAction
import com.dashlane.item.v3.data.LoadingFormData
import com.dashlane.item.v3.loaders.AsyncDataLoader
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.util.MenuActionHelper
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.util.SensitiveField.PASSWORD
import com.dashlane.item.v3.util.SensitiveFieldLoader
import com.dashlane.item.v3.util.buildInfoBoxToDisplay
import com.dashlane.item.v3.util.revealPassword
import com.dashlane.item.v3.util.updateFormData
import com.dashlane.item.v3.util.updateMenuActions
import com.dashlane.item.v3.viewmodels.State.Companion.emptyState
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncState.Idle
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class ItemEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val dataSync: DataSync,
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private val sensitiveFieldLoader: SensitiveFieldLoader,
    private val vaultItemCopy: VaultItemCopyService,
    private val itemEditRepository: ItemEditRepository,
    private val appEvents: AppEvents,
    private val menuActionHelper: MenuActionHelper,
    private val specializedFormBuilder: SpecializedFormBuilder,
    private val asyncDataLoader: AsyncDataLoader,
    internal val frozenStateManager: FrozenStateManager,
    internal val navigator: Navigator,
) : ViewModel() {
    protected val navArgs = ItemEditFragmentArgs.fromSavedStateHandle(savedStateHandle)
    protected val mutableUiState = MutableStateFlow(emptyState(navArgs.uid, navArgs.forceEdit))
    protected lateinit var initialFormData: FormData
    val uiState = mutableUiState.asStateFlow()

    init {
        loadForm()
    }

    fun showSensitiveField(field: SensitiveField) {
        mutableUiState.update { loadSensitiveField(state = it, field = field) }
    }

    fun hideSensitiveField(field: SensitiveField) {
        mutableUiState.update { hideSensitiveField(state = it, field = field) }
    }

    fun updateFormDataFromView(formData: FormData) {
        mutableUiState.updateFormData { _ -> formData }
        updateMenuActions()
    }

    fun saveData() {
        viewModelScope.launch {
            mutableUiState.update {
                if (!hasUnsavedChanges(it)) {
                    
                    return@update goToViewMode(it)
                }
                
                val initialVault = loadVaultItem(it.formData.id) ?: createItem()
                val data = itemEditRepository.save(initialVault, it.formData)

                
                initialFormData = data
                val state = it.copy(formData = data, isNew = false, itemAction = ItemAction.Saved)
                return@update goToViewMode(state)
            }
        }
    }

    fun onCloseClicked(fromDiscardButton: Boolean) {
        viewModelScope.launch {
            mutableUiState.update {
                if (it.isEditMode) {
                    if (hasUnsavedChanges(it) && !fromDiscardButton) {
                        
                        it.copy(itemAction = ItemAction.ConfirmSaveChanges)
                    } else {
                        if (it.isNew) {
                            
                            it.copy(itemAction = ItemAction.Close)
                        } else {
                            
                            switchMode(it.copy(formData = initialFormData))
                        }
                    }
                } else {
                    it.copy(itemAction = ItemAction.Close)
                }
            }
        }
    }

    fun onBackPressed() = onCloseClicked(false)

    fun closeAfterRemoved(delayBeforeClose: Long = 0L) {
        viewModelScope.launch {
            
            delay(delayBeforeClose)
            mutableUiState.update { it.copy(itemAction = ItemAction.Close) }
        }
    }

    fun copyToClipboard(copyField: CopyField) {
        viewModelScope.launch {
            loadSummaryObject(mutableUiState.value.formData.id)?.let {
                vaultItemCopy.handleCopy(item = it, copyField = copyField)
            }
        }
    }

    fun actionOpenCollection(
        collections: List<CollectionData>,
        teamId: String?,
        isSharedWithLimitedRight: Boolean
    ) {
        if (frozenStateManager.isAccountFrozen) {
            navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }
        viewModelScope.launch {
            mutableUiState.update { state ->
                state.copy(
                    itemAction = ItemAction.OpenCollection(
                        temporaryPrivateCollectionsName = collections.filter { !it.shared }
                            .map { it.name },
                        temporarySharedCollectionsId = collections.filter { it.shared }
                            .mapNotNull { it.id },
                        spaceId = teamId ?: TeamSpace.Personal.teamId ?: "",
                        isLimited = isSharedWithLimitedRight
                    )
                )
            }
        }
    }

    fun actionOpenNoRights() =
        mutableUiState.update { it.copy(itemAction = ItemAction.OpenNoRights) }

    fun actionOpenShared() = mutableUiState.update { it.copy(itemAction = ItemAction.OpenShared) }

    fun actionGeneratePassword() {
        val origin = if (mutableUiState.value.isNew) {
            PasswordGeneratorDialog.CREATION_VIEW
        } else {
            PasswordGeneratorDialog.EDIT_VIEW
        }
        mutableUiState.update { it.copy(itemAction = ItemAction.OpenPasswordGenerator(origin)) }
    }

    fun actionDelete() = mutableUiState.update { it.copy(itemAction = ItemAction.ConfirmDelete) }

    fun actionHandled() = mutableUiState.update { it.copy(itemAction = null) }

    private fun loadForm() {
        viewModelScope.launch {
            val firstLoad = (mutableUiState.value.formData as? LoadingFormData)?.firstLoad ?: false
            val initialSummaryObject =
                mutableUiState.value.formData.id.takeIf { it.isNotEmpty() }?.let {
                    loadSummaryObject(it)
                } ?: createItem().toSummary()
            val asyncLoader = asyncDataLoader.get(initialSummaryObject::class)
            asyncLoader?.cancelAll()
            
            val formData = specializedFormBuilder.get(initialSummaryObject::class).build(
                initialSummaryObject,
                mutableUiState.value,
            )

            
            mutableUiState.update {
                val state = it.copy(formData = formData)
                
                if (it.isEditMode) goToEditMode(state) else state
            }

            
            asyncLoader?.loadAsync(
                initialSummaryObject,
                isNewItem = mutableUiState.value.isNew,
                scope = this@launch,
                additionalDataLoadedFunction = mutableUiState::updateFormData.also { updateMenuActions() },
                onAllDataLoaded = ::onAllDataLoaded
            ) ?: onAllDataLoaded()
            updateMenuActions()
            if (firstLoad) registerForEvents()
        }
    }

    private suspend fun onAllDataLoaded() {
        initialFormData = mutableUiState.value.formData
        mutableUiState.update {
            it.buildInfoBoxToDisplay(
                isFrozenState = frozenStateManager.isAccountFrozen,
                passwordLimitCount = frozenStateManager.passwordLimitCount
            )
        }
        
        loadVaultItem(mutableUiState.value.formData.id)?.let { vaultItem ->
            if (vaultItem.hasBeenSaved && (!mutableUiState.value.isEditMode || vaultItem.syncObjectType == SyncObjectType.SECURE_NOTE)) {
                itemEditRepository.setItemViewed(vaultItem)
            }
        }
    }

    private fun switchMode(state: State): State {
        val newValue = state.isEditMode.not()
        return if (newValue) goToEditMode(state) else goToViewMode(state)
    }

    private fun goToViewMode(state: State): State {
        val menuActions = getMenuActions(state = state, editMode = false)
        return hideSensitiveField(state, PASSWORD).copy(
            isEditMode = false,
            menuActions = menuActions
        )
    }

    private fun goToEditMode(state: State): State {
        val menuActions = getMenuActions(state = state, editMode = true)
        return loadSensitiveField(state, PASSWORD).copy(
            isEditMode = true,
            menuActions = menuActions
        )
    }

    private fun loadSensitiveField(state: State, field: SensitiveField): State {
        
        val id = state.formData.id
        val revealedFields = state.revealedFields + field
        
        return when (field) {
            PASSWORD -> state.revealPassword(sensitiveFieldLoader, id, field, revealedFields).also {
                if (state.formData is CredentialFormData && state.formData.password == null) {
                    
                    initialFormData = it.formData
                }
            }
        }
    }

    private fun hideSensitiveField(state: State, field: SensitiveField) =
        state.copy(revealedFields = state.revealedFields - field)

    private fun createItem(): VaultItem<*> {
        mutableUiState.update {
            it.copy(
                isEditMode = true,
                isNew = true,
                revealedFields = SensitiveField.entries.toSet()
            )
        }
        return createNewItem()
    }

    protected abstract fun createNewItem(): VaultItem<*>

    protected fun loadSummaryObject(uid: String) = genericDataQuery.queryFirst(GenericFilter(uid))

    protected fun loadVaultItem(uid: String) =
        vaultDataQuery.queryLegacy(vaultFilter { specificUid(uid) })

    protected fun isProSpace(vaultItem: VaultItem<*>): Boolean {
        return vaultItem.isSpaceItem() && vaultItem.syncObject.spaceId.isNotSemanticallyNull()
    }

    protected fun updateMenuActions() = mutableUiState.updateMenuActions(
        menuActions = { state, editMode -> getMenuActions(state, editMode) }
    )

    private fun getMenuActions(state: State, editMode: Boolean) = menuActionHelper.getMenuActions(
        mutableUiState,
        state,
        editMode,
        saveAction = { saveData() },
        switchModeAction = { switchMode(it) }
    )

    private fun registerForEvents() {
        
        appEvents.registerAsFlow(
            this@ItemEditViewModel,
            clazz = DataIdentifierReplacedEvent::class.java,
            deliverLastEvent = false
        ).mapNotNull { event ->
            if (event.oldItemId == mutableUiState.value.formData.id) {
                mutableUiState.update { emptyState(event.newItemId, it.isEditMode) }
                loadForm()
            }
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        
        viewModelScope.launch {
            dataSync.dataSyncState.collect { dataSyncState ->
                val state = mutableUiState.value
                if (state.itemAction != null || state.isNew) return@collect
                if (dataSyncState != Idle.Success) return@collect
                if (loadSummaryObject(state.formData.id) != null) return@collect
                
                closeAfterRemoved()
            }
        }
    }

    private fun hasUnsavedChanges(state: State) = initialFormData != state.formData || state.isNew
}

data class State(
    val formData: FormData,
    val isNew: Boolean,
    val revealedFields: Set<SensitiveField>,
    val isEditMode: Boolean,
    val menuActions: List<MenuAction>,
    val itemAction: ItemAction?,
    val infoBoxes: List<InfoBoxData>
) {
    companion object {
        fun emptyState(
            itemId: String?,
            isEditMode: Boolean,
            firstLoad: Boolean = false
        ) = State(
            formData = LoadingFormData(itemId ?: "", firstLoad),
            isNew = itemId == null,
            revealedFields = emptySet(),
            isEditMode = isEditMode,
            menuActions = emptyList(),
            itemAction = null,
            infoBoxes = emptyList()
        )
    }
}
