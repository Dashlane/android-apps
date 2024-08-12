package com.dashlane.teamspaces.db

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.sharing.model.isAccepted
import com.dashlane.teamspaces.manager.SpaceDeletedNotifier
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.manager.matchForceDomains
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("LargeClass")
@Singleton
class SmartSpaceCategorizationManager @Inject constructor(
    @ApplicationCoroutineScope
    coroutineScope: CoroutineScope,
    private val databaseAccessor: SmartSpaceCategorizationDatabaseAccessor,
    private val sharingDataProvider: SharingDataProvider,
    private val spaceDeletedNotifier: SpaceDeletedNotifier,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val teamspaceForceDeletionSharingWorker: TeamspaceForceDeletionSharingWorker,
    private val activityLogger: VaultActivityLogger
) {

    
    @OptIn(ObsoleteCoroutinesApi::class)
    private val actor =
        coroutineScope.actor<Unit>(context = Dispatchers.IO, capacity = Channel.CONFLATED) {
            consumeEach {
                executeInternal()
            }
        }

    suspend fun executeSync() {
        actor.send(Unit)
    }

    fun executeAsync() {
        actor.trySend(Unit)
    }

    @WorkerThread
    @VisibleForTesting
    suspend fun executeInternal() {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return
        val currentTeam = teamSpaceAccessor.currentBusinessTeam
        val pastTeams = teamSpaceAccessor.pastBusinessTeams
        val result = moveVaultItems(currentTeam, pastTeams)

        result.itemsDeleted.forEach {
            activityLogger.sendAuthentifiantActivityLog(vaultItem = it, action = Action.DELETE)
        }
        result.itemsForced.forEach {
            activityLogger.sendAuthentifiantActivityLog(vaultItem = it, action = Action.ADD)
        }

        teamspaceForceDeletionSharingWorker.revokeAll(result.idsSharedToRevoked)

        markNotifyServerContentDeletedIfRequire(pastTeams)
    }

    private suspend fun moveVaultItems(currentBusinessSpace: TeamSpace.Business.Current?, pastTeams: List<TeamSpace.Business.Past>): Result {
        val forceCategorizedItems: MutableList<VaultItem<SyncObject>> = mutableListOf()
        val deletedItems: MutableList<VaultItem<SyncObject>> = mutableListOf()
        val sharedItemIdsToRevoke: MutableList<String> = mutableListOf()

        
        moveCollectionItemsToBusinessSpace(currentBusinessSpace, forceCategorizedItems)

        
        if (currentBusinessSpace?.isForcedDomainsEnabled == true) {
            applyForceCategorization(currentBusinessSpace, forceCategorizedItems)
        }

        
        pastTeams.forEach { pastTeam ->
            handleItemFromRevokedTeam(pastTeam, deletedItems, sharedItemIdsToRevoke)
        }
        return Result(forceCategorizedItems, deletedItems, sharedItemIdsToRevoke)
    }

    private suspend fun moveCollectionItemsToBusinessSpace(
        currentBusinessSpace: TeamSpace.Business.Current?,
        itemsForced: MutableList<VaultItem<SyncObject>>
    ) {
        val itemGroups = sharingDataProvider.getItemGroups()
        val currentBusinessTeamId = currentBusinessSpace?.teamId ?: return
        val itemIdsToForce = itemGroups.mapNotNull { group ->
            if (group.collections?.any { it.isAccepted } == true) group.items?.map { it.itemId } else null
        }.flatten()

        
        if (itemIdsToForce.isEmpty()) return

        val vaultItems = databaseAccessor.getVaultItems(itemIdsToForce)
        
        val editedVaultItems = vaultItems.mapNotNull { item ->
            if (item.syncObject.spaceId == currentBusinessTeamId) return@mapNotNull null
            item.copyWithAttrs {
                teamSpaceId = currentBusinessTeamId
                syncState = SyncState.MODIFIED
            }
        }
        
        if (editedVaultItems.isNotEmpty()) {
            databaseAccessor.save(editedVaultItems)
        }
        itemsForced.addAll(editedVaultItems)
    }

    private suspend fun applyForceCategorization(
        currentTeam: TeamSpace.Business.Current,
        itemsForced: MutableList<VaultItem<SyncObject>>,
    ) {
        val currentTeamId: String = currentTeam.teamId
        val domains: List<String> = currentTeam.domains

        val itemsToMove: List<SummaryObject> = getItemsForForcedCategorization(domains)
        moveForceCategorizationItemsToSpace(itemsToMove, currentTeamId)
            .also { itemsForced.addAll(it) }
    }

    private suspend fun handleItemFromRevokedTeam(
        pastTeam: TeamSpace.Business.Past,
        itemsDeleted: MutableList<VaultItem<SyncObject>>,
        idsSharedToRevoked: MutableList<String>
    ) {
        
        
        if (pastTeam.isForcedDomainsEnabled && pastTeam.isRemovedBusinessContentEnabled) {
            val forcedItems: List<SummaryObject> = getItemsForForcedCategorization(pastTeam.domains)
            idsSharedToRevoked.addAll(getUidsSharedItemForceCategorization(forcedItems))
        }

        
        if (!pastTeam.isRemovedBusinessContentEnabled) {
            moveItemsToPersonalSpace(databaseAccessor.getSummaryItemsForSpace(pastTeam))
        }

        
        if (pastTeam.shouldDelete && pastTeam.isRemovedBusinessContentEnabled) {
            val teamItems = databaseAccessor.getSummaryItemsForSpace(pastTeam)
            
            deleteTeamItems(teamItems)
                .also { itemsDeleted.addAll(it) }
        }
    }

    private fun getUidsSharedItemForceCategorization(itemsShouldForced: List<SummaryObject>): List<String> {
        return itemsShouldForced.mapNotNull {
            if (it.isShared) {
                it.id
            } else {
                null
            }
        }
    }

    private suspend fun moveItemsToPersonalSpace(teamItems: List<SummaryObject>) {
        
        val ids = teamItems.filterNot {
            it.syncState == SyncState.DELETED ||
            it.syncState == SyncState.IN_SYNC_DELETED
        }.map {
            it.id
        }
        
        val vaultItems = databaseAccessor.getVaultItems(ids)
            .ifEmpty { return }

        
        val modifiedItems = vaultItems.map { vault ->
            vault.copyWithAttrs {
                teamSpaceId = null
                syncState = SyncState.MODIFIED
            }
        }
        
        databaseAccessor.save(modifiedItems)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun deleteTeamItems(
        teamItems: List<SummaryObject>
    ): List<VaultItem<SyncObject>> {
        val ids = teamItems.map { it.id }
            .ifEmpty { return emptyList() }

        
        val vaultItems = databaseAccessor.getVaultItems(ids)
            .ifEmpty { return emptyList() }

        
        val deletedItems = vaultItems.map { vault ->
            vault.copyWithAttrs { syncState = SyncState.DELETED }
        }
        
        databaseAccessor.save(deletedItems)

        
        val authentifiants = vaultItems.mapNotNull { it as? VaultItem<SyncObject.Authentifiant> }
        if (authentifiants.isNotEmpty()) {
            deleteGeneratedPasswords(authentifiants)
        }
        return deletedItems
    }

    private suspend fun deleteGeneratedPasswords(authentifiants: List<VaultItem<SyncObject.Authentifiant>>) {
        
        val matchingGeneratedPasswords = databaseAccessor.getGeneratedPasswords().filter { generatedPassword ->
            authentifiants.firstOrNull { authentifiant ->
                authentifiant.syncObject.password == generatedPassword.syncObject.password
            } != null
        }
        
        val deletedGeneratedPasswords = matchingGeneratedPasswords.map { vault ->
            vault.copyWithAttrs { syncState = SyncState.DELETED }
        }
        databaseAccessor.save(deletedGeneratedPasswords)
    }

    private suspend fun moveForceCategorizationItemsToSpace(
        itemsToMove: List<SummaryObject>,
        teamId: String
    ): List<VaultItem<SyncObject>> {
        
        val ids = itemsToMove
            .filter { it.spaceId != teamId }
            .map { it.id }

        
        val vaultItems = databaseAccessor.getVaultItems(ids)

        
        val editedVaultItems = vaultItems.map { vault ->
            vault.copyWithAttrs {
                teamSpaceId = teamId
                syncState = SyncState.MODIFIED
            }
        }

        
        databaseAccessor.save(editedVaultItems)
        return editedVaultItems
    }

    private fun markNotifyServerContentDeletedIfRequire(teamspaces: List<TeamSpace.Business.Past>) {
        teamspaces.forEach { space ->
            if (space.shouldDelete) {
                markNotifyServerSpaceDeleted(space)
            }
        }
    }

    private fun markNotifyServerSpaceDeleted(spaceToDeleted: TeamSpace.Business.Past) {
        spaceDeletedNotifier.storeSpaceToDelete(spaceToDeleted)
    }

    private fun getItemsForForcedCategorization(
        domains: List<String>,
    ): List<SummaryObject> {
        
        val items: List<SummaryObject> = databaseAccessor.getSummaryCandidatesForCategorization()

        return items.matchForceDomains(domains)
    }

    private data class Result(
        val itemsForced: List<VaultItem<SyncObject>>,
        val itemsDeleted: List<VaultItem<SyncObject>>,
        val idsSharedToRevoked: List<String>
    )
}
