package com.dashlane.item.v3.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.dashlane.authenticator.Hotp
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.UriParser
import com.dashlane.events.AppEvents
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.item.v3.builders.CredentialBuilder
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.ItemAction
import com.dashlane.item.v3.data.getItemLinkedServices
import com.dashlane.item.v3.loaders.CredentialAsyncDataLoader
import com.dashlane.item.v3.repositories.CollectionsRepository
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.repositories.PasswordHealthRepository
import com.dashlane.item.v3.util.MenuActionHelper
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.util.SensitiveFieldLoader
import com.dashlane.item.v3.util.buildInfoBoxToDisplay
import com.dashlane.item.v3.util.updateFormData
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.navigation.Navigator
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorDialog
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createAuthentifiant
import com.dashlane.vault.model.getDefaultName
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CredentialItemEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dataSync: DataSync,
    genericDataQuery: GenericDataQuery,
    vaultDataQuery: VaultDataQuery,
    appEvents: AppEvents,
    menuActionHelper: MenuActionHelper,
    credentialBuilder: CredentialBuilder,
    asyncDataLoader: CredentialAsyncDataLoader,
    vaultItemCopy: VaultItemCopyService,
    private val sensitiveFieldLoader: SensitiveFieldLoader,
    private val vaultItemLogger: VaultItemLogger,
    private val itemEditRepository: ItemEditRepository,
    collectionsRepository: CollectionsRepository,
    private val passwordHealthRepository: PasswordHealthRepository,
    sharingPolicyDataProvider: SharingPolicyDataProvider,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    frozenStateManager: FrozenStateManager,
    navigator: Navigator,
) : ItemEditViewModel<CredentialFormData>(
    savedStateHandle = savedStateHandle,
    dataSync = dataSync,
    genericDataQuery = genericDataQuery,
    vaultDataQuery = vaultDataQuery,
    sensitiveFieldLoader = sensitiveFieldLoader,
    vaultItemCopy = vaultItemCopy,
    itemEditRepository = itemEditRepository,
    appEvents = appEvents,
    menuActionHelper = menuActionHelper,
    formDataBuilder = credentialBuilder,
    asyncDataLoader = asyncDataLoader,
    frozenStateManager = frozenStateManager,
    navigator = navigator,
    collectionsRepository = collectionsRepository,
    sharingPolicyDataProvider = sharingPolicyDataProvider
) {
    override val mutableUiState: MutableViewStateFlow<ItemEditState<CredentialFormData>, ItemEditSideEffect> =
        MutableViewStateFlow(
            ItemEditState(
                itemId = navArgs.uid ?: "",
                isNew = navArgs.uid == null,
                isEditMode = navArgs.forceEdit
            )
        )

    private var passwordHealthJob: Job? = null

    fun updatePassword(password: String, generatedPasswordId: String? = null) {
        mutableUiState.updateFormData {
            it.copy(
                password = CredentialFormData.Password(
                    SyncObfuscatedValue(password),
                    generatedPasswordId
                )
            )
        }
        updateMenuActions()
        viewModelScope.launch {
            
            passwordHealthJob?.cancel()
            passwordHealthJob = viewModelScope.launch(ioDispatcher) {
                val passwordHealth = passwordHealthRepository.getPasswordHealth(password = password)
                mutableUiState.updateFormData {
                    it.copy(passwordHealth = passwordHealth)
                }
                updateMenuActions()
                mutableUiState.update {
                    it.buildInfoBoxToDisplay(
                        isFrozenState = frozenStateManager.isAccountFrozen,
                        passwordLimitCount = frozenStateManager.passwordLimitCount
                    )
                }
            }
        }
    }

    fun handlePasswordRestoreResult(success: Boolean) {
        viewModelScope.launch {
            mutableUiState.send(ItemEditSideEffect.PasswordRestoreResult(success))
        }
        if (success) {
            val password = mutableUiState.value.itemId?.let { itemId ->
                sensitiveFieldLoader.getSensitiveField(itemId, SensitiveField.PASSWORD)
            }?.toString() ?: return
            updatePassword(password)
        }
    }

    fun actionGeneratePassword() {
        viewModelScope.launch {
            val origin = if (mutableUiState.value.isNew) {
                PasswordGeneratorDialog.CREATION_VIEW
            } else {
                PasswordGeneratorDialog.EDIT_VIEW
            }
            mutableUiState.value.datas?.current?.let {
                mutableUiState.send(
                    ItemEditSideEffect.OpenPasswordGenerator(
                        origin = origin,
                        domainAsking = it.formData.url ?: ""
                    )
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun refreshHotp(hotp: Hotp) {
        viewModelScope.launch {
            val newHotp = UriParser.incrementHotpCounter(hotp)
            mutableUiState.value.itemId?.let { id ->
                loadVaultItem(id)
            }?.let { item ->
                itemEditRepository.updateOtp(item as VaultItem<SyncObject.Authentifiant>, newHotp)
                mutableUiState.update {
                    it.copyCurrentFormData { current ->
                        current.copy(otp = newHotp)
                    }.copyInitialFormData { initial, current ->
                        initial.copy(otp = current.otp)
                    }
                }
                updateMenuActions()
            }
        }
    }

    fun update2FA(otp: Otp) {
        mutableUiState.updateFormData {
            it.copy(otp = otp)
        }
        updateMenuActions()
    }

    fun updateLinkedServices(linkedWebsites: List<String>?, linkedApplications: List<String>?) =
        mutableUiState.updateFormData {
            it.copy(
                linkedServices = it.linkedServices.copy(
                    addedByUserDomains = linkedWebsites ?: it.linkedServices.addedByUserDomains,
                    addedByUserApps = linkedApplications ?: it.linkedServices.addedByUserApps
                )
            )
        }.also { updateMenuActions() }

    fun reloadLinkedServices() {
        viewModelScope.launch {
            val summaryObject = loadSummaryObject(
                mutableUiState.value.itemId
            ) as? SummaryObject.Authentifiant ?: return@launch
            mutableUiState.updateFormData {
                it.copy(linkedServices = summaryObject.getItemLinkedServices())
            }
            updateMenuActions()
        }
    }

    fun actionOpenWebsite() {
        viewModelScope.launch {
            val state = mutableUiState.value
            if (state.datas == null || state.itemId == null) return@launch
            val goToWebsiteUrl =
                (loadSummaryObject(state.itemId) as? SummaryObject.Authentifiant)?.urlForGoToWebsite ?: return@launch
            val loginListener = object : LoginOpener.Listener {
                override fun onShowOption() = Unit

                override fun onLogin(packageName: String) {
                    vaultItemLogger.logOpenExternalLink(
                        itemId = state.itemId,
                        packageName = packageName,
                        url = goToWebsiteUrl
                    )
                }
            }
            val packageNames = state.datas.current.formData.linkedServices.addedByUserApps.toSet() +
                state.datas.current.formData.linkedServices.addedByDashlaneApps.toSet()
            mutableUiState.send(
                ItemEditSideEffect.OpenWebsite(
                    goToWebsiteUrl,
                    packageNames,
                    loginListener
                )
            )
        }
    }

    fun actionOpenGuidedPasswordChange() {
        viewModelScope.launch {
            loadSummaryObject(mutableUiState.value.itemId)?.let { summary ->
                if (summary !is SummaryObject.Authentifiant) {
                    return@let
                }
                val url = summary.urlForGoToWebsite ?: return@let
                mutableUiState.value.datas?.current?.let {
                    mutableUiState.send(
                        ItemEditSideEffect.GuidedPasswordChange(it.commonData.id, url, summary.loginForUi)
                    )
                }
            }
        }
    }

    fun actionOpenLinkedServices(addNew: Boolean) {
        viewModelScope.launch {
            mutableUiState.value.datas?.current?.let {
                mutableUiState.send(
                    ItemEditSideEffect.OpenLinkedServices(
                        id = it.commonData.id,
                        fromViewOnly = !mutableUiState.value.isEditMode,
                        addNew = addNew,
                        temporaryWebsites = it.formData.linkedServices.addedByUserDomains,
                        temporaryApps = it.formData.linkedServices.addedByUserApps,
                        url = it.formData.url
                    )
                )
            }
        }
    }

    fun actionSetupTwoFactor() {
        viewModelScope.launch {
            val state = mutableUiState.value
            if (state.datas == null) return@launch
            val vaultItem = loadVaultItem(state.itemId)
            mutableUiState.send(
                ItemEditSideEffect.GoToSetup2FA(
                    credentialName = state.datas.current.commonData.name,
                    credentialId = state.datas.current.commonData.id,
                    topDomain = state.datas.current.formData.url?.toUrlDomainOrNull()?.root.toString(),
                    packageName = (state.datas.current.formData.linkedServices.addedByUserApps + state.datas.current.formData.linkedServices.addedByDashlaneApps).firstOrNull(),
                    proSpace = vaultItem != null && vaultItem.isSpaceItem() && vaultItem.syncObject.spaceId.isNotSemanticallyNull()
                )
            )
        }
    }

    fun actionRemoveTwoFactorConfirmed() {
        viewModelScope.launch {
            val vaultItem = loadVaultItem(mutableUiState.value.itemId) ?: return@launch
            itemEditRepository.remove2FAToken(vaultItem, isProSpace(vaultItem))
            mutableUiState.updateFormData { it.copy(otp = null) }
            updateMenuActions()
        }
    }

    fun actionRemoveTwoFactor() =
        mutableUiState.update { it.copy(itemAction = ItemAction.ConfirmRemove2FA) }

    override fun onCleared() {
        super.onCleared()
        passwordHealthJob?.cancel()
    }

    override fun createNewItem() = createAuthentifiant(
        title = SyncObject.Authentifiant.getDefaultName(navArgs.url),
        deprecatedUrl = navArgs.url,
        autoLogin = "true",
        passwordModificationDate = Instant.now()
    )
}