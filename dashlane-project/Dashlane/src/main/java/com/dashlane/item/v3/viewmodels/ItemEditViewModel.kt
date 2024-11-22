package com.dashlane.item.v3.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.events.AppEvents
import com.dashlane.events.DataIdentifierReplacedEvent
import com.dashlane.events.registerAsFlow
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.v3.ItemEditFragmentArgs
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.InfoBoxData
import com.dashlane.item.v3.data.ItemAction
import com.dashlane.item.v3.loaders.AsyncDataLoader
import com.dashlane.item.v3.repositories.CollectionsRepository
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.util.MenuActionHelper
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.util.SensitiveField.PASSWORD
import com.dashlane.item.v3.util.SensitiveFieldLoader
import com.dashlane.item.v3.util.buildInfoBoxToDisplay
import com.dashlane.item.v3.util.revealPassword
import com.dashlane.item.v3.util.updateCommonData
import com.dashlane.item.v3.util.updateMenuActions
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.State
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.navigation.Navigator
import com.dashlane.securefile.AttachmentsParser
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncState.Idle
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Suppress("LargeClass")
abstract class ItemEditViewModel<T : FormData>(
    savedStateHandle: SavedStateHandle,
    private val dataSync: DataSync,
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private val sensitiveFieldLoader: SensitiveFieldLoader,
    private val vaultItemCopy: VaultItemCopyService,
    private val itemEditRepository: ItemEditRepository,
    private val appEvents: AppEvents,
    private val menuActionHelper: MenuActionHelper,
    private val formDataBuilder: FormData.Builder<T>,
    private val asyncDataLoader: AsyncDataLoader<T>?,
    internal val frozenStateManager: FrozenStateManager,
    internal val navigator: Navigator,
    private val collectionsRepository: CollectionsRepository,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
) : ViewModel() {
    protected val navArgs = ItemEditFragmentArgs.fromSavedStateHandle(savedStateHandle)

    protected abstract val mutableUiState: MutableViewStateFlow<ItemEditState<T>, ItemEditSideEffect>
    val uiState: ViewStateFlow<ItemEditState<T>, ItemEditSideEffect> by lazy { mutableUiState }

    fun viewStarted() {
        if (mutableUiState.value.datas == null) loadForm()
    }

    fun showSensitiveField(field: SensitiveField) {
        mutableUiState.update { loadSensitiveField(state = it, field = field) }
    }

    fun hideSensitiveField(field: SensitiveField) {
        mutableUiState.update { hideSensitiveField(state = it, field = field) }
    }

    fun updateCurrentDataFromView(data: Data<T>) {
        mutableUiState.update {
            it.copy(datas = it.datas?.copy(current = data))
        }
        updateMenuActions()
    }

    fun updateCommonDataFromView(value: CommonData) {
        mutableUiState.updateCommonData { value }
        updateMenuActions()
    }

    fun saveData() {
        viewModelScope.launch {
            mutableUiState.update {
                if (it.datas?.current == null || !hasUnsavedChanges(it)) {
                    
                    return@update goToViewMode(it)
                }
                if (hasError(it.datas.current)) {
                    return@update it.copy(itemAction = ItemAction.SaveError)
                }
                
                val initialVault = it.itemId?.let { uid -> loadVaultItem(uid) } ?: createItem()
                val isNewItem = !initialVault.hasBeenSaved
                val uid = itemEditRepository.save(initialVault, it.datas.current)

                val state = it.copyCurrentCommonData { commonData ->
                    commonData.copy(
                        id = uid,
                        created = commonData.created ?: Instant.now(),
                        updated = Instant.now(),
                        canDelete = isNewItem || commonData.canDelete
                    )
                }.copyInitialCommonData { _, current ->
                    current
                }.copy(
                    isNew = false,
                    itemId = uid
                ).buildInfoBoxToDisplay(
                    isFrozenState = frozenStateManager.isAccountFrozen,
                    passwordLimitCount = frozenStateManager.passwordLimitCount
                ).also {
                    viewModelScope.launch {
                        mutableUiState.send(ItemEditSideEffect.Saved)
                    }
                }
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
                            
                            switchMode(it.copy(datas = it.datas?.copy(current = it.datas.initial)))
                        }
                    }
                } else {
                    it.copy(itemAction = ItemAction.Close)
                }
            }
        }
    }

    fun onBackPressed() = onCloseClicked(false)

    private fun closeAfterRemoved(delayBeforeClose: Long = 0L) {
        viewModelScope.launch {
            delay(delayBeforeClose)
            mutableUiState.update { it.copy(itemAction = ItemAction.Close) }
        }
    }

    fun copyToClipboard(copyField: CopyField) {
        viewModelScope.launch {
            val datas = mutableUiState.value.datas ?: return@launch 
            loadSummaryObject(datas.current.commonData.id)?.let {
                vaultItemCopy.handleCopy(item = it, copyField = copyField)
            }
        }
    }

    fun actionOpenCollection() {
        if (frozenStateManager.isAccountFrozen) {
            viewModelScope.launch {
                mutableUiState.send(ItemEditSideEffect.FrozenPaywall)
            }
            return
        }

        val commonData = mutableUiState.value.datas?.current?.commonData
        commonData?.collections?.let { collections ->
            viewModelScope.launch {
                mutableUiState.send(
                    ItemEditSideEffect.OpenCollection(
                        temporaryPrivateCollectionsName = collections.filter { !it.shared }.map { it.name },
                        temporarySharedCollectionsId = collections.filter { it.shared }.mapNotNull { it.id },
                        spaceId = commonData.space?.teamId,
                        isLimited = commonData.isSharedWithLimitedRight
                    )
                )
            }
        }
    }

    fun actionOpenNoRights() =
        mutableUiState.update { it.copy(itemAction = ItemAction.OpenNoRights) }

    fun actionOpenShared() =
        viewModelScope.launch {
            mutableUiState.value.datas?.current?.let {
                mutableUiState.send(ItemEditSideEffect.OpenShared(it.commonData.id))
            }
        }

    fun actionDelete() = viewModelScope.launch {
        mutableUiState.value.datas?.current?.let {
            mutableUiState.send(
                ItemEditSideEffect.ConfirmDelete(
                    it.commonData.id,
                    it.commonData.isShared
                )
            )
        }
    }

    fun actionHandled() = mutableUiState.update { it.copy(itemAction = null) }

    fun addCollection(collection: CollectionData) {
        mutableUiState.updateCommonData {
            it.copy(collections = (it.collections ?: emptyList()) + collection)
        }
        updateMenuActions()
        viewModelScope.launch {
            mutableUiState.update {
                
                if (it.isEditMode) {
                    it
                } else {
                    it.datas ?: return@update it
                    val initialVault = loadVaultItem(it.itemId) ?: return@launch
                    it.datas.current.commonData.collections?.let { collections ->
                        collectionsRepository.saveCollections(initialVault, collections)
                    }
                    
                    it.copyInitialCommonData { initial, current ->
                        initial.copy(
                            collections = current.collections
                        )
                    }.buildInfoBoxToDisplay(
                        isFrozenState = frozenStateManager.isAccountFrozen,
                        passwordLimitCount = frozenStateManager.passwordLimitCount
                    ).also {
                        viewModelScope.launch {
                            mutableUiState.send(ItemEditSideEffect.Saved)
                        }
                    }
                }
            }
        }
    }

    fun removeCollection(collection: CollectionData) {
        mutableUiState.updateCommonData {
            it.copy(collections = it.collections?.minus(collection))
        }
        updateMenuActions()
    }

    fun onViewAttachments() {
        viewModelScope.launch {
            val summary = loadSummaryObject(mutableUiState.value.itemId)
            mutableUiState.send(ItemEditSideEffect.ShowAttachments(summary))
        }
    }

    fun handleSharingResult() {
        viewModelScope.launch {
            val state = mutableUiState.value
            if (!state.isNew && loadVaultItem(state.itemId) == null) {
                
                closeAfterRemoved()
                return@launch
            }
            mutableUiState.update {
                it.copyCurrentCommonData {
                    val sharingCount = FormData.SharingCount(sharingPolicyDataProvider.getSharingCount(it.id))
                    it.copy(
                        sharingCount = sharingCount,
                        isShared = sharingCount.userCount > 0 || sharingCount.groupCount > 0
                    )
                }.buildInfoBoxToDisplay(
                    isFrozenState = frozenStateManager.isAccountFrozen,
                    passwordLimitCount = frozenStateManager.passwordLimitCount
                )
            }
            updateMenuActions()
        }
    }

    private fun loadForm() {
        viewModelScope.launch {
            val firstLoad = mutableUiState.value.datas == null
            val initialSummaryObject =
                mutableUiState.value.itemId?.takeIf { it.isNotEmpty() }?.let {
                    loadSummaryObject(it)
                } ?: createItem().toSummary()
            asyncDataLoader?.cancelAll()
            
            val data =
                formDataBuilder.build(initialSummaryObject = initialSummaryObject, state = mutableUiState.value)

            
            mutableUiState.update {
                val state = it.copy(datas = Datas(current = data, initial = data))
                
                if (it.isEditMode) goToEditMode(state) else state
            }

            
            asyncDataLoader?.loadAsync(
                initialSummaryObject,
                isNewItem = mutableUiState.value.isNew,
                scope = this@launch,
                additionalDataLoadedFunction = ::additionalDataLoaded,
                onAllDataLoaded = ::onAllDataLoaded
            ) ?: onAllDataLoaded()
            updateMenuActions()
            if (firstLoad) registerForEvents()
        }
    }

    private fun additionalDataLoaded(block: (Data<T>) -> Data<T>) {
        mutableUiState.update {
            it.copyCurrent {
                block(it)
            }
        }.also {
            updateMenuActions()
        }
    }

    private suspend fun onAllDataLoaded() {
        mutableUiState.update {
            it.copyInitial { _, current -> current }
                .buildInfoBoxToDisplay(
                    isFrozenState = frozenStateManager.isAccountFrozen,
                    passwordLimitCount = frozenStateManager.passwordLimitCount
                )
        }
        
        mutableUiState.value.itemId?.let { itemId ->
            loadVaultItem(itemId)
        }?.let { vaultItem ->
            if (vaultItem.hasBeenSaved && (!mutableUiState.value.isEditMode || vaultItem.syncObjectType == SyncObjectType.SECURE_NOTE)) {
                itemEditRepository.setItemViewed(vaultItem)
            }
        }
    }

    private fun switchMode(state: ItemEditState<T>): ItemEditState<T> {
        val newValue = state.isEditMode.not()
        return if (newValue) goToEditMode(state) else goToViewMode(state)
    }

    private fun goToViewMode(state: ItemEditState<T>): ItemEditState<T> {
        val menuActions = getMenuActions(state = state, editMode = false)
        return hideSensitiveField(state, PASSWORD).copy(
            isEditMode = false,
            menuActions = menuActions
        )
    }

    private fun goToEditMode(state: ItemEditState<T>): ItemEditState<T> {
        val menuActions = getMenuActions(state = state, editMode = true)
        return loadSensitiveField(state, PASSWORD).copy(
            isEditMode = true,
            menuActions = menuActions
        )
    }

    private fun loadSensitiveField(state: ItemEditState<T>, field: SensitiveField): ItemEditState<T> {
        
        val id = state.itemId ?: return state
        val revealedFields = state.revealedFields + field

        
        return when (field) {
            PASSWORD -> {
                val formData = state.datas?.current?.formData
                if (formData is CredentialFormData) {
                    state.revealPassword(sensitiveFieldLoader, id, field, revealedFields)
                } else {
                    state
                }
            }
        }
    }

    private fun hideSensitiveField(state: ItemEditState<T>, field: SensitiveField) =
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

    protected fun loadSummaryObject(uid: String?) = uid?.let { genericDataQuery.queryFirst(GenericFilter(uid)) }

    protected fun loadVaultItem(uid: String?) =
        uid?.let { vaultDataQuery.queryLegacy(vaultFilter { specificUid(uid) }) }

    protected fun isProSpace(vaultItem: VaultItem<*>): Boolean {
        return vaultItem.isSpaceItem() && vaultItem.syncObject.spaceId.isNotSemanticallyNull()
    }

    protected fun updateMenuActions() = mutableUiState.updateMenuActions(
        menuActions = { state, editMode -> getMenuActions(state, editMode) }
    )

    private fun getMenuActions(state: ItemEditState<T>, editMode: Boolean) = menuActionHelper.getMenuActions(
        uiState = mutableUiState,
        coroutineScope = viewModelScope,
        state = state,
        isEditMode = editMode,
        saveAction = { saveData() },
        switchModeAction = { switchMode(it) }
    )

    private fun registerForEvents() {
        
        appEvents.registerAsFlow(
            this@ItemEditViewModel,
            clazz = DataIdentifierReplacedEvent::class.java,
            deliverLastEvent = false
        ).mapNotNull { event ->
            if (event.oldItemId == mutableUiState.value.datas?.current?.commonData?.id) {
                mutableUiState.update {
                    ItemEditState(
                        isNew = false,
                        isEditMode = it.isEditMode
                    )
                }
                loadForm()
            }
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

        
        dataSync.dataSyncState.onEach { dataSyncState ->
            val state = mutableUiState.value
            if (state.itemAction != null || state.isNew) return@onEach
            if (dataSyncState != Idle.Success) return@onEach
            if (state.datas?.current?.commonData?.id != null && loadSummaryObject(state.datas.current.commonData.id) != null) return@onEach
            
            closeAfterRemoved()
        }.launchIn(viewModelScope)
    }

    private fun hasUnsavedChanges(state: ItemEditState<T>) = state.datas?.initial != state.datas?.current || state.isNew

    protected open fun hasError(current: Data<T>): Boolean = false

    protected fun ItemEditState<T>.copyCurrentCommonData(block: (current: CommonData) -> CommonData): ItemEditState<T> =
        copyCurrent { current ->
            current.copy(
                commonData = block(current.commonData)
            )
        }

    protected fun ItemEditState<T>.copyCurrentFormData(block: (current: T) -> T): ItemEditState<T> =
        copyCurrent { current ->
            current.copy(
                formData = block(current.formData)
            )
        }

    protected fun ItemEditState<T>.copyInitialCommonData(block: (initial: CommonData, current: CommonData) -> CommonData): ItemEditState<T> =
        copyInitial { initial, current ->
            initial.copy(
                commonData = block(initial.commonData, current.commonData)
            )
        }

    protected fun ItemEditState<T>.copyInitialFormData(block: (initial: T, current: T) -> T): ItemEditState<T> =
        copyInitial { initial, current ->
            initial.copy(
                formData = block(initial.formData, current.formData)
            )
        }

    protected fun ItemEditState<T>.copyCurrent(block: (Data<T>) -> Data<T>): ItemEditState<T> = copy(
        datas = datas?.copy(
            current = block(datas.current)
        )
    )

    private fun ItemEditState<T>.copyInitial(block: (initial: Data<T>, current: Data<T>) -> Data<T>): ItemEditState<T> =
        copy(
        datas = datas?.copy(
            initial = block(datas.initial, datas.current)
        )
    )

    fun onAttachmentsChanged(attachments: String?) {
        val attachmentCount = AttachmentsParser().parse(attachments).size
        mutableUiState.update {
            it.copy(
                datas = it.datas?.copy(
                    current = it.datas.current.copyCommonData { it.copy(attachmentCount = attachmentCount) },
                    initial = it.datas.initial.copyCommonData { it.copy(attachmentCount = attachmentCount) }
                )
            ).buildInfoBoxToDisplay( 
                isFrozenState = frozenStateManager.isAccountFrozen,
                passwordLimitCount = frozenStateManager.passwordLimitCount
            )
        }.also {
            updateMenuActions()
        }
    }

    fun onVaultItemDeleted(itemId: String?) {
        if (mutableUiState.value.itemId == itemId) {
            
            
            closeAfterRemoved(200)
        }
    }
}

data class ItemEditState<T : FormData>(
    val itemId: String? = null,
    val datas: Datas<T>? = null,
    val isNew: Boolean,
    val revealedFields: Set<SensitiveField> = emptySet(),
    val isEditMode: Boolean,
    val menuActions: List<MenuAction> = emptyList(),
    val itemAction: ItemAction? = null,
    val infoBoxes: List<InfoBoxData> = emptyList()
) : State.View {
    fun toNonNullableState(): FormState<T> = FormState(
        itemId = itemId!!,
        datas = datas!!,
        isNew = isNew,
        revealedFields = revealedFields,
        isEditMode = isEditMode,
        menuActions = menuActions,
        itemAction = itemAction,
        infoBoxes = emptyList()
    )
}

data class FormState<T : FormData>(
    val itemId: String? = null,
    val datas: Datas<T>,
    val isNew: Boolean,
    val revealedFields: Set<SensitiveField> = emptySet(),
    val isEditMode: Boolean,
    val menuActions: List<MenuAction> = emptyList(),
    val itemAction: ItemAction? = null,
    val infoBoxes: List<InfoBoxData> = emptyList()
)

data class Data<T>(
    val formData: T,
    val commonData: CommonData
) {
    fun copyFormData(block: (formData: T) -> T): Data<T> = copy(formData = block(formData))
    fun copyCommonData(block: (CommonData) -> CommonData): Data<T> = copy(commonData = block(commonData))
}

data class Datas<T>(
    val current: Data<T>,
    val initial: Data<T>
)

sealed class ItemEditSideEffect : State.SideEffect {
    data object Close : ItemEditSideEffect()

    data class ConfirmDelete(val id: String, val isShared: Boolean) : ItemEditSideEffect()

    data class OpenShared(val id: String) : ItemEditSideEffect()

    data class ShowAttachments(val summaryObject: SummaryObject?) : ItemEditSideEffect()

    data class OpenPasswordHistory(val id: String) : ItemEditSideEffect()

    data class GuidedPasswordChange(
        val id: String,
        val website: String,
        val userName: String?
    ) : ItemEditSideEffect()

    data class GoToSetup2FA(
        val credentialName: String,
        val credentialId: String,
        val topDomain: String,
        val packageName: String?,
        val proSpace: Boolean
    ) : ItemEditSideEffect()

    data class OpenPasswordGenerator(val origin: String, val domainAsking: String) : ItemEditSideEffect()

    data class OpenWebsite(
        val url: String,
        val packageNames: Set<String>,
        val listener: LoginOpener.Listener
    ) : ItemEditSideEffect()

    data class OpenCollection(
        val temporaryPrivateCollectionsName: List<String>,
        val temporarySharedCollectionsId: List<String>,
        val spaceId: String?,
        val isLimited: Boolean
    ) : ItemEditSideEffect()

    data class OpenLinkedServices(
        val id: String,
        val fromViewOnly: Boolean,
        val addNew: Boolean,
        val temporaryWebsites: List<String>,
        val temporaryApps: List<String>?,
        val url: String?
    ) : ItemEditSideEffect()

    data object Saved : ItemEditSideEffect()

    data class PasswordRestoreResult(val success: Boolean) : ItemEditSideEffect()

    data object FrozenPaywall : ItemEditSideEffect()
}