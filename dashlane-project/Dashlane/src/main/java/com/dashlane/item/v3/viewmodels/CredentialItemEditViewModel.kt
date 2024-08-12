package com.dashlane.item.v3.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.dashlane.authenticator.Hotp
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.UriParser
import com.dashlane.events.AppEvents
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.item.v3.builders.SpecializedFormBuilder
import com.dashlane.item.v3.data.CollectionData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.ItemAction
import com.dashlane.item.v3.data.getItemLinkedServices
import com.dashlane.item.v3.loaders.AsyncDataLoader
import com.dashlane.item.v3.repositories.CollectionsRepository
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.repositories.PasswordHealthRepository
import com.dashlane.item.v3.util.MenuActionHelper
import com.dashlane.item.v3.util.SensitiveField
import com.dashlane.item.v3.util.SensitiveFieldLoader
import com.dashlane.item.v3.util.buildInfoBoxToDisplay
import com.dashlane.item.v3.util.updateCredentialFormData
import com.dashlane.navigation.Navigator
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.isNotSemanticallyNull
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CredentialItemEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    dataSync: DataSync,
    genericDataQuery: GenericDataQuery,
    vaultDataQuery: VaultDataQuery,
    appEvents: AppEvents,
    menuActionHelper: MenuActionHelper,
    specializedFormBuilder: SpecializedFormBuilder,
    asyncDataLoader: AsyncDataLoader,
    vaultItemCopy: VaultItemCopyService,
    private val sensitiveFieldLoader: SensitiveFieldLoader,
    private val vaultItemLogger: VaultItemLogger,
    private val itemEditRepository: ItemEditRepository,
    private val collectionsRepository: CollectionsRepository,
    private val passwordHealthRepository: PasswordHealthRepository,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    frozenStateManager: FrozenStateManager,
    navigator: Navigator,
) : ItemEditViewModel(
    savedStateHandle,
    dataSync,
    genericDataQuery,
    vaultDataQuery,
    sensitiveFieldLoader,
    vaultItemCopy,
    itemEditRepository,
    appEvents,
    menuActionHelper,
    specializedFormBuilder,
    asyncDataLoader,
    frozenStateManager,
    navigator,
) {

    private var passwordHealthJob: Job? = null

    fun updatePassword(password: String, generatedPasswordId: String? = null) {
        mutableUiState.updateCredentialFormData {
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
                mutableUiState.updateCredentialFormData {
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
        val state = mutableUiState.updateAndGet {
            it.copy(itemAction = ItemAction.PasswordRestoreResult(success))
        }
        if (success) {
            val password =
                sensitiveFieldLoader.getSensitiveField(state.formData.id, SensitiveField.PASSWORD)
                    ?.toString()
                    ?: return
            updatePassword(password)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun refreshHotp(hotp: Hotp) {
        viewModelScope.launch {
            val newHotp = UriParser.incrementHotpCounter(hotp)
            loadVaultItem(mutableUiState.value.formData.id)?.let { item ->
                itemEditRepository.updateOtp(item as VaultItem<SyncObject.Authentifiant>, newHotp)
                mutableUiState.updateCredentialFormData {
                    val result = it.copy(otp = newHotp)
                    initialFormData = result
                    result
                }
                updateMenuActions()
            }
        }
    }

    fun update2FA(otp: Otp) {
        mutableUiState.updateCredentialFormData {
            it.copy(otp = otp)
        }
        updateMenuActions()
    }

    
    fun addCollection(collection: CollectionData) {
        mutableUiState.updateCredentialFormData {
            it.copy(collections = it.collections + collection)
        }
        updateMenuActions()
        viewModelScope.launch {
            mutableUiState.update {
                
                if (it.isEditMode) {
                    it
                } else {
                    val data = it.formData as? CredentialFormData ?: return@update it
                    val initialVault = loadVaultItem(it.formData.id) ?: return@launch
                    collectionsRepository.saveCollections(initialVault, data)
                    
                    initialFormData = data
                    it.copy(isNew = false, itemAction = ItemAction.Saved)
                }
            }
        }
    }

    
    fun removeCollection(collection: CollectionData) {
        mutableUiState.updateCredentialFormData {
            it.copy(collections = it.collections - collection)
        }
        updateMenuActions()
    }

    
    fun handleSharingResult() {
        viewModelScope.launch {
            val state = mutableUiState.value
            if (!state.isNew && loadVaultItem(state.formData.id) == null) {
                
                closeAfterRemoved()
                return@launch
            }
            mutableUiState.updateCredentialFormData {
                val sharingCount =
                    FormData.SharingCount(sharingPolicyDataProvider.getSharingCount(it.id))
                it.copy(sharingCount = sharingCount)
            }
            updateMenuActions()
        }
    }

    fun updateLinkedServices(linkedWebsites: List<String>?, linkedApplications: List<String>?) =
        mutableUiState.updateCredentialFormData {
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
                mutableUiState.value.formData.id
            ) as? SummaryObject.Authentifiant ?: return@launch
            mutableUiState.updateCredentialFormData {
                it.copy(linkedServices = summaryObject.getItemLinkedServices())
            }
            updateMenuActions()
        }
    }

    fun actionOpenWebsite() {
        viewModelScope.launch {
            mutableUiState.update {
                val goToWebsiteUrl =
                    (loadSummaryObject(it.formData.id) as? SummaryObject.Authentifiant)?.urlForGoToWebsite
                        ?: return@launch
                if (it.formData !is CredentialFormData) return@update it
                val loginListener = object : LoginOpener.Listener {
                    override fun onShowOption() = Unit

                    override fun onLogin(packageName: String) {
                        vaultItemLogger.logOpenExternalLink(
                            itemId = it.formData.id,
                            packageName = packageName,
                            url = goToWebsiteUrl
                        )
                    }
                }
                val packageNames =
                    it.formData.linkedServices.addedByUserApps.toSet() + it.formData.linkedServices.addedByDashlaneApps.toSet()
                it.copy(
                    itemAction = ItemAction.OpenWebsite(
                        goToWebsiteUrl,
                        packageNames,
                        loginListener
                    )
                )
            }
        }
    }

    fun actionOpenGuidedPasswordChange() {
        viewModelScope.launch {
            loadSummaryObject(mutableUiState.value.formData.id)?.let {
                if (it !is SummaryObject.Authentifiant) {
                    return@let
                }
                val url = it.urlForGoToWebsite ?: return@let
                mutableUiState.update { state ->
                    state.copy(itemAction = ItemAction.GuidedPasswordChange(url, it.loginForUi))
                }
            }
        }
    }

    fun actionOpenLinkedServices(addNew: Boolean) {
        mutableUiState.update {
            if (it.formData !is CredentialFormData) return@update it
            it.copy(
                itemAction = ItemAction.OpenLinkedServices(
                    fromViewOnly = !mutableUiState.value.isEditMode,
                    addNew = addNew,
                    temporaryWebsites = it.formData.linkedServices.addedByUserDomains,
                    temporaryApps = it.formData.linkedServices.addedByUserApps,
                    url = it.formData.url
                )
            )
        }
    }

    fun actionSetupTwoFactor() {
        viewModelScope.launch {
            mutableUiState.update {
                if (it.formData !is CredentialFormData) return@update it
                val vaultItem = loadVaultItem(it.formData.id)
                it.copy(
                    itemAction = ItemAction.GoToSetup2FA(
                        credentialName = it.formData.name,
                        credentialId = it.formData.id,
                        topDomain = it.formData.url?.toUrlDomainOrNull()?.root.toString(),
                        packageName = (it.formData.linkedServices.addedByUserApps + it.formData.linkedServices.addedByDashlaneApps).firstOrNull(),
                        proSpace = vaultItem != null && vaultItem.isSpaceItem() && vaultItem.syncObject.spaceId.isNotSemanticallyNull()
                    )
                )
            }
        }
    }

    fun actionRemoveTwoFactorConfirmed() {
        viewModelScope.launch {
            val vaultItem = loadVaultItem(mutableUiState.value.formData.id) ?: return@launch
            itemEditRepository.remove2FAToken(vaultItem, isProSpace(vaultItem))
            mutableUiState.updateCredentialFormData { it.copy(otp = null) }
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