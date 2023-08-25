package com.dashlane.item.linkedwebsites

import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.core.DataSync
import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.util.matchDomain
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.getTeamSpaceLog
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import javax.inject.Inject

class LinkedServicesDataProvider @Inject constructor(
    private val mainDataAccessor: MainDataAccessor,
    private val vaultItemLogger: VaultItemLogger,
    private val activityLogger: VaultActivityLogger,
    private val teamspaceForceCategorizationManager: TeamspaceForceCategorizationManager,
    private val linkedServicesHelper: LinkedServicesHelper,
    private val dataSync: DataSync
) : LinkedServicesContract.DataProvider {

    private val dataSaver = mainDataAccessor.getDataSaver()

    @Suppress("UNCHECKED_CAST")
    override suspend fun getItem(itemId: String): VaultItem<SyncObject.Authentifiant>? {
        val query = vaultFilter { specificUid(itemId) }
        return mainDataAccessor.getVaultDataQuery().query(query) as? VaultItem<SyncObject.Authentifiant>
    }

    override suspend fun save(
        vaultItem: VaultItem<SyncObject.Authentifiant>,
        linkedWebsites: List<String>,
        linkedApps: List<String>
    ): Boolean {
        val websites = linkedServicesHelper.replaceAllLinkedDomains(
            vaultItem.syncObject.linkedServices,
            linkedWebsites
        ).associatedDomains
        val apps = linkedServicesHelper.replaceAllLinkedAppsByUser(
            vaultItem.syncObject.linkedServices,
            linkedApps
        ).associatedAndroidApps
        val toSaveItem = vaultItem.copySyncObject {
            this.linkedServices = SyncObject.Authentifiant.LinkedServices(apps, websites)
        }.copyWithAttrs {
            userModificationDate = Instant.now()
            syncState = SyncState.MODIFIED
        }
        val isSaved = runCatching { dataSaver.save(toSaveItem) }.getOrElse { false }
        val updatedLinkedWebsites = toSaveItem.getUpdatedLinkedWebsites(vaultItem)
        val removedApps = toSaveItem.getRemovedLinkedApps(vaultItem)
        val action = Action.EDIT
        vaultItemLogger.logUpdate(
            action,
            getEditedField(
                updatedLinkedWebsites?.first,
                updatedLinkedWebsites?.second,
                removedApps
            ),
            vaultItem.uid,
            ItemType.CREDENTIAL,
            space = vaultItem.getTeamSpaceLog(),
            url = (vaultItem.syncObject as? SyncObject.Authentifiant)?.urlForGoToWebsite,
            addedWebsites = updatedLinkedWebsites?.first,
            removedWebsites = updatedLinkedWebsites?.second,
            removedApps = removedApps
        )
        activityLogger.sendActivityLog(vaultItem = vaultItem, action = action)

        
        teamspaceForceCategorizationManager.executeSync()

        
        dataSync.sync(Trigger.SAVE)

        return isSaved
    }

    private fun getEditedField(
        addedWebsites: List<String>?,
        removedWebsites: List<String>?,
        removedApps: List<String>?
    ): List<Field> {
        return mutableListOf<Field>().apply {
            if (!addedWebsites.isNullOrEmpty() || !removedWebsites.isNullOrEmpty()) {
                add(Field.ASSOCIATED_WEBSITES_LIST)
            }
            if (!removedApps.isNullOrEmpty()) {
                add(Field.ASSOCIATED_APPS_LIST)
            }
        }
    }

    override fun getDuplicateWebsitesItem(
        vaultItem: VaultItem<SyncObject.Authentifiant>?,
        websites: List<String>
    ): Pair<String, String>? {
        
        val addedWebsites = getUpdatedLinkedWebsites(
            websites,
            vaultItem?.syncObject?.linkedServices?.associatedDomains?.map { it.domain ?: "" }
        ).first

        checkOtherCredentialsDuplicate(addedWebsites, vaultItem?.syncObject)?.let {
            return it
        }
        return checkSelfDuplicate(addedWebsites, vaultItem?.syncObject)
    }

    private fun checkOtherCredentialsDuplicate(
        addedWebsites: List<String>,
        syncObject: SyncObject.Authentifiant?
    ): Pair<String, String>? {
        val allAuthentifiant = mainDataAccessor.getGenericDataQuery()
            .queryAll(vaultFilter { specificDataType(SyncObjectType.AUTHENTIFIANT) })
            .filterNot { it.id == syncObject?.id }
            .filterIsInstance<SummaryObject.Authentifiant>()
        addedWebsites.forEach { website ->
            allAuthentifiant.firstOrNull {
                val isAutomaticallyAddedWebsitesDuplicate =
                    KnownLinkedDomains.getMatchingLinkedDomainSet(it.urlDomain)
                        ?.any { linkedDomain -> linkedDomain.value.matchDomain(website) } == true
                isAutomaticallyAddedWebsitesDuplicate || credentialHasWebsite(it, website)
            }?.let {
                return Pair(it.title ?: "", website)
            }
        }
        return null
    }

    private fun checkSelfDuplicate(
        addedWebsites: List<String>,
        syncObject: SyncObject.Authentifiant?
    ): Pair<String, String>? {
        addedWebsites.forEach { website ->
            val isAutomaticallyAddedWebsitesDuplicate =
                KnownLinkedDomains.getMatchingLinkedDomainSet(syncObject?.urlDomain)
                    ?.any { linkedDomain -> linkedDomain.value.matchDomain(website) } == true
            if (isAutomaticallyAddedWebsitesDuplicate || 
                syncObject?.urlForUI()?.matchDomain(website) == true || 
                addedWebsites.count { it == website } > 1 
            ) {
                return Pair(syncObject?.title ?: "", website)
            }
        }
        return null
    }

    private fun credentialHasWebsite(credential: SummaryObject.Authentifiant, website: String) =
        credential.urlForUI()?.matchDomain(website) == true ||
                credential.linkedServices?.associatedDomains?.any { linkedService ->
                    linkedService.domain.matchDomain(website)
                } == true
}