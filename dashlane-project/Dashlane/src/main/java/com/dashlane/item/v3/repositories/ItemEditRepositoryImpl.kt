package com.dashlane.item.v3.repositories

import android.content.Context
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.Hotp
import com.dashlane.authenticator.otp
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.item.linkedwebsites.getRemovedLinkedApps
import com.dashlane.item.linkedwebsites.getUpdatedLinkedWebsites
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.util.ItemEditValueUpdateManager
import com.dashlane.item.v3.util.fillDefaultValue
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.similarpassword.SimilarPassword
import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.DataChangeHistoryFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.getTeamSpaceLog
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.useractivity.hermes.TrackingLogUtils
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.history.DataChangeHistoryField
import com.dashlane.vault.history.password
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getAllLinkedPackageName
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.toItemType
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

class ItemEditRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataSync: DataSync,
    private val dataSaver: DataSaver,
    private val authenticatorLogger: AuthenticatorLogger,
    private val generatedPasswordRepository: GeneratedPasswordRepository,
    private val newItemRepository: NewItemRepository,
    private val collectionsRepository: CollectionsRepository,
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val currentTeamSpaceFilter: CurrentTeamSpaceUiFilter,
    private val dataChangeHistoryQuery: DataChangeHistoryQuery,
    private val sessionManager: SessionManager,
    private val itemEditValueUpdateManager: ItemEditValueUpdateManager,
    private val vaultItemLogger: VaultItemLogger,
    private val activityLogger: VaultActivityLogger,
    private val vaultDataQuery: VaultDataQuery,
) : ItemEditRepository {
    private val teamSpaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceAccessorProvider.get()
    private val session: Session?
        get() = sessionManager.session

    private val similarPassword = SimilarPassword()

    @Suppress("LongMethod")
    override suspend fun save(initialVault: VaultItem<SyncObject>, data: FormData): FormData {
        val isNewItem = !initialVault.hasBeenSaved

        
        var itemToSave = itemEditValueUpdateManager.updateWithData(data, initialVault)
            ?: throw Error("Unsupported type: $this")

        
        itemToSave = itemToSave.fillDefaultValue(context, session)

        
        val updatedLinkedWebsites = itemToSave.getUpdatedLinkedWebsites(initialVault)

        
        val removedLinkedApps = itemToSave.getRemovedLinkedApps(initialVault)

        
        dataSaver.save(itemToSave)

        if (isNewItem) newItemRepository.performAdditionalSteps(itemToSave)

        
        logItemUpdate(
            item = itemToSave,
            action = if (isNewItem) {
                Action.ADD
            } else {
                Action.EDIT
            },
            addedWebsites = updatedLinkedWebsites?.first,
            removedWebsites = updatedLinkedWebsites?.second,
            removedApps = removedLinkedApps,
            collectionCount = data.collections.size,
            editedFields = itemEditValueUpdateManager.editedFields.toList()
        )

        
        if (data is CredentialFormData) {
            collectionsRepository.saveCollections(itemToSave, data)
            generatedPasswordRepository.updateGeneratedPassword(data, itemToSave)
        }

        
        dataSync.sync(Trigger.SAVE)
        return when (data) {
            is CredentialFormData -> data.copy(
                id = itemToSave.uid,
                created = data.created ?: Instant.now(),
                updated = Instant.now(),
                canDelete = if (isNewItem) true else data.canDelete
            )
            else -> data
        }
    }

    override suspend fun updateOtp(vaultItem: VaultItem<SyncObject.Authentifiant>, hotp: Hotp) {
        val updatedItem = vaultItem.copy(
            syncObject = vaultItem.syncObject.copy {
                otpUrl = hotp.url?.toSyncObfuscatedValue() ?: SyncObfuscatedValue("")
                otpSecret = if (hotp.isStandardOtp()) {
                    hotp.secret?.toSyncObfuscatedValue()
                } else {
                    null
                } ?: SyncObfuscatedValue("")
            }
        ).copyWithAttrs {
            syncState = SyncState.MODIFIED
        }
        dataSaver.save(updatedItem)
        authenticatorLogger.logUpdateCredential(updatedItem.uid)
    }

    override suspend fun getPasswordReusedCount(password: String): Int {
        val filter = vaultFilter { specificDataType(SyncObjectType.AUTHENTIFIANT) }
        return vaultDataQuery.queryAll(filter)
            .filterIsInstance<VaultItem<SyncObject.Authentifiant>>()
            .mapNotNull { it.syncObject.password?.toString() }
            .count { similarPassword.areSimilar(password, it) }
    }

    override fun getTeamspace(spaceId: String?): TeamSpace? {
        if (teamSpaceAccessor?.canChangeTeamspace != true) return null
        return spaceId?.let {
            
            teamSpaceAccessor?.availableSpaces?.firstOrNull { it.teamId == spaceId }
                ?: TeamSpace.Personal 
        } 
            ?: currentTeamSpaceFilter.currentFilter.teamSpace.takeUnless { it == TeamSpace.Combined }
            
            ?: TeamSpace.Personal 
    }

    override fun hasPasswordHistory(authentifiant: VaultItem<SyncObject.Authentifiant>): Boolean {
        val filter = DataChangeHistoryFilter(objectType = SyncObjectType.AUTHENTIFIANT, objectUid = authentifiant.uid)
        val changeSets = dataChangeHistoryQuery.query(filter)?.syncObject?.changeSets?.filter {
            it.changedProperties?.contains(DataChangeHistoryField.PASSWORD.field) ?: false
        }?.sortedByDescending { it.modificationDate } ?: return false

        return changeSets.filterIndexed { index, item ->
            
            (index == 0 && item.password != authentifiant.syncObject.password.toString() && item.password != null && item.modificationDate != null) ||
                
                (index != 0 && item.password != null && changeSets[index - 1].modificationDate != null)
        }.isNotEmpty()
    }

    override fun remove2FAToken(vaultItem: VaultItem<*>, isProSpace: Boolean) {
        val syncObject = vaultItem.syncObject as? SyncObject.Authentifiant ?: return
        val packageName = syncObject.toSummary<SummaryObject.Authentifiant>()
            .linkedServices.getAllLinkedPackageName()
            .firstOrNull()
        val topDomain = syncObject.url?.toUrlDomainOrNull()?.root.toString()
        val domain = TrackingLogUtils.createDomainForLog(topDomain, packageName)
        val issuer = syncObject.otp()?.issuer
        authenticatorLogger.setup(isProSpace, domain).logRemove2fa(issuer)
    }

    override suspend fun setItemViewed(vaultItem: VaultItem<SyncObject>) {
        dataSaver.save(
            vaultItem.copyWithAttrs {
                locallyViewedDate = Instant.now()
                locallyUsedCount = vaultItem.locallyUsedCount + 1
            }
        )
    }

    private fun logItemUpdate(
        item: VaultItem<*>,
        action: Action,
        addedWebsites: List<String>?,
        removedWebsites: List<String>?,
        removedApps: List<String>?,
        collectionCount: Int?,
        editedFields: List<Field>?
    ) {
        vaultItemLogger.logUpdate(
            action = action,
            editedFields = editedFields?.takeIf { action == Action.EDIT }?.toList(),
            itemId = item.uid,
            itemType = item.syncObjectType.toItemType(),
            space = item.getTeamSpaceLog(),
            url = (item.syncObject as? SyncObject.Authentifiant)?.urlForGoToWebsite,
            addedWebsites = addedWebsites,
            removedWebsites = removedWebsites,
            removedApps = removedApps,
            collectionCount = collectionCount
        )
        activityLogger.sendAuthentifiantActivityLog(vaultItem = item, action = action)
    }
}