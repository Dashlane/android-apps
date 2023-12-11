package com.dashlane.teamspaces.db

import androidx.annotation.WorkerThread
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.sharing.model.isAccepted
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoRestrictionSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.CombinedTeamspace
import com.dashlane.teamspaces.PersonalTeamspace
import com.dashlane.teamspaces.manager.SpaceDeletedNotifier
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.TeamspaceMatcher
import com.dashlane.teamspaces.manager.matchForceDomains
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.screens.fragments.userdata.sharing.center.SharingDataProvider
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.vault.VaultActivityLogger
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
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
class TeamspaceForceCategorizationManager @Inject constructor(
    @ApplicationCoroutineScope
    coroutineScope: CoroutineScope,
    private val mainDataAccessor: MainDataAccessor,
    private val sharingDataProvider: SharingDataProvider,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val spaceDeletedNotifier: SpaceDeletedNotifier,
    private val teamspaceForceDeletionSharingWorker: TeamspaceForceDeletionSharingWorker,
    private val activityLogger: VaultActivityLogger
) {
    private val dataSaver: DataSaver
        get() = mainDataAccessor.getDataSaver()
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    
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
    private suspend fun executeInternal() {
        val teamspaces: MutableList<Teamspace> = ArrayList()
        val teamspaceAccessor = teamspaceAccessorProvider.get() ?: return
        teamspaces.addAll(teamspaceAccessor.all)
        teamspaces.addAll(teamspaceAccessor.revokedAndDeclinedSpaces)

        val result = moveVaultItems(teamspaces)

        result.itemsDeleted.forEach {
            activityLogger.sendActivityLog(vaultItem = it, action = Action.DELETE)
        }
        result.itemsForced.forEach {
            activityLogger.sendActivityLog(vaultItem = it, action = Action.ADD)
        }

        teamspaceForceDeletionSharingWorker.revokeAll(result.idsSharedToRevoked)

        markNotifyServerContentDeletedIfRequire(teamspaces)
    }

    private suspend fun moveVaultItems(teamspaces: List<Teamspace?>): Result {
        val itemsForced: MutableList<VaultItem<SyncObject>> = mutableListOf()
        val itemsDeleted: MutableList<VaultItem<SyncObject>> = mutableListOf()
        val idsSharedToRevoked: MutableList<String> = mutableListOf()
        moveCollectionItemsToBusinessSpace(teamspaces, itemsForced)
        teamspaces.forEach { teamspace ->
            teamspace?.teamId ?: return@forEach

            if (!teamspace.isDomainRestrictionsEnable) {
                return@forEach
            }
            if (teamspace.domains.isEmpty()) {
                return@forEach
            }

            moveVaultItemsForTeamspace(teamspace, itemsForced, itemsDeleted, idsSharedToRevoked)
        }
        return Result(itemsForced, itemsDeleted, idsSharedToRevoked)
    }

    private suspend fun moveCollectionItemsToBusinessSpace(
        teamspaces: List<Teamspace?>,
        itemsForced: MutableList<VaultItem<SyncObject>>
    ) {
        val itemGroups = sharingDataProvider.getItemGroups()
        val businessSpaceId = teamspaces
            .minus(setOf(CombinedTeamspace, PersonalTeamspace))
            .firstOrNull()
            ?.teamId ?: return
        val itemIdsToForce = itemGroups.mapNotNull { group ->
            if (group.collections?.any { it.isAccepted } == true) group.items?.map { it.itemId } else null
        }.flatten()
        val vaultItems = getVaultItems(SyncObjectType.AUTHENTIFIANT, itemIdsToForce)
        
        val editedVaultItems = vaultItems.mapNotNull { item ->
            if (item.syncObject.spaceId == businessSpaceId) return@mapNotNull null
            item.copyWithAttrs {
                teamSpaceId = businessSpaceId
                syncState = SyncState.MODIFIED
            }
        }
        
        if (editedVaultItems.isNotEmpty()) dataSaver.save(editedVaultItems)
        itemsForced.addAll(editedVaultItems)
    }

    private suspend fun moveVaultItemsForTeamspace(
        teamspace: Teamspace,
        itemsForced: MutableList<VaultItem<SyncObject>>,
        itemsDeleted: MutableList<VaultItem<SyncObject>>,
        idsSharedToRevoked: MutableList<String>
    ) {
        val teamId: String = teamspace.teamId ?: return
        val domains: List<String> = teamspace.domains
        val teamspaceStatus = teamspace.status
        val dataTypes = TeamspaceMatcher.DATA_TYPE_TO_MATCH
        dataTypes.forEach { dataType ->
            val itemsShouldForced: List<SummaryObject> = getItemsShouldForced(domains, dataType)
            if (Teamspace.Status.ACCEPTED == teamspaceStatus) {
                
                moveForceCategorizationItemsToSpace(itemsShouldForced, teamId, dataType)
                    .also { itemsForced.addAll(it) }
            } else if (Teamspace.Status.REVOKED == teamspaceStatus) {
                if (teamspace.shouldDeleteForceCategorizedContent()) {
                    
                    deleteForceCategorizationItems(itemsShouldForced, dataType)
                        .also { itemsDeleted.addAll(it) }
                    
                    
                    moveNonForceCategorizationItemsToPersonal(domains, teamId, dataType)
                }
                
                
                if (teamspace.isDomainRestrictionsEnable &&
                    teamspace.isRemoveForcedContentEnabled
                ) {
                    idsSharedToRevoked.addAll(getUidsSharedItemForceCategorization(itemsShouldForced))
                }
            }
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

    private suspend fun moveNonForceCategorizationItemsToPersonal(
        domains: List<String>,
        teamId: String,
        dataType: SyncObjectType
    ) {
        
        val items: List<SummaryObject> = getSummaryItems(dataType)
        val ids = items.mapNotNull { summaryObject ->
            if (summaryObject.spaceId == teamId && !summaryObject.matchForceDomains(domains)) {
                summaryObject.id
            } else {
                null
            }
        }
        
        val vaultItems = getVaultItems(dataType, ids)

        
        val editedVaultItems = vaultItems.map { vault ->
            vault.copyWithAttrs {
                teamSpaceId = ""
                syncState = SyncState.MODIFIED
            }
        }
        
        dataSaver.save(editedVaultItems)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun deleteForceCategorizationItems(
        itemsShouldForced: List<SummaryObject>,
        dataType: SyncObjectType
    ): List<VaultItem<SyncObject>> {
        
        val ids = itemsShouldForced.map { summaryObject ->
            summaryObject.id
        }
        
        val vaultItems = getVaultItems(dataType, ids)
        
        val editedVaultItems = vaultItems.map { vault ->
            vault.copyWithAttrs { syncState = SyncState.DELETED }
        }
        
        dataSaver.save(editedVaultItems)

        
        if (dataType == SyncObjectType.AUTHENTIFIANT) {
            vaultItems as List<VaultItem<SyncObject.Authentifiant>>
            deleteGeneratedPasswords(vaultItems)
        }
        return editedVaultItems
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun deleteGeneratedPasswords(itemsShouldForced: List<VaultItem<SyncObject.Authentifiant>>) {
        
        val vaultFilter = vaultFilter {
            specificDataType(SyncObjectType.GENERATED_PASSWORD)
        }
        val generatedPasswords = vaultDataQuery.queryAll(vaultFilter)
            .map { it as VaultItem<SyncObject.GeneratedPassword> }

        
        val items = generatedPasswords.filter {
            itemsShouldForced.firstOrNull { authentifiant ->
                authentifiant.syncObject.password == it.syncObject.password
            } != null
        }
        
        val editedVaultItems = items.map { vault ->
            vault.copyWithAttrs { syncState = SyncState.DELETED }
        }
        dataSaver.save(editedVaultItems)
    }

    private suspend fun moveForceCategorizationItemsToSpace(
        itemsShouldForced: List<SummaryObject>,
        teamId: String,
        dataType: SyncObjectType
    ): List<VaultItem<SyncObject>> {
        
        val ids = itemsShouldForced.mapNotNull { summaryObject ->
            if (summaryObject.spaceId != teamId) {
                summaryObject.id
            } else {
                null
            }
        }
        
        val vaultItems = getVaultItems(dataType, ids)

        
        val editedVaultItems = vaultItems.map { vault ->
            vault.copyWithAttrs {
                teamSpaceId = teamId
                syncState = SyncState.MODIFIED
            }
        }

        
        dataSaver.save(editedVaultItems)
        return editedVaultItems
    }

    private fun getItemsShouldForced(
        domains: List<String>,
        dataType: SyncObjectType
    ): List<SummaryObject> {
        
        val items: List<SummaryObject> = getSummaryItems(dataType)
        return items.filter { summaryObject ->
            summaryObject.matchForceDomains(domains)
        }
    }

    private fun getSummaryItems(dataType: SyncObjectType): List<SummaryObject> {
        val filter = genericFilter {
            specificDataType(dataType)
            spaceFilter = NoRestrictionSpaceFilter
            allStatusFilter()
        }
        return genericDataQuery.queryAll(filter)
    }

    private fun getVaultItems(
        dataType: SyncObjectType,
        ids: List<String>
    ): List<VaultItem<SyncObject>> {
        val vaultFilter = vaultFilter {
            specificDataType(dataType)
            specificUid(ids)
            spaceFilter = NoRestrictionSpaceFilter
            allStatusFilter()
        }
        return vaultDataQuery.queryAll(vaultFilter)
    }

    private fun markNotifyServerContentDeletedIfRequire(teamspaces: List<Teamspace>) {
        teamspaces.forEach { space ->
            if (Teamspace.Status.REVOKED == space.status && space.shouldDelete()) {
                markNotifyServerSpaceDeleted(space.teamId)
            }
        }
    }

    private fun markNotifyServerSpaceDeleted(teamId: String?) {
        spaceDeletedNotifier.storeSpaceToDelete(teamId)
    }

    private data class Result(
        val itemsForced: List<VaultItem<SyncObject>>,
        val itemsDeleted: List<VaultItem<SyncObject>>,
        val idsSharedToRevoked: List<String>
    )
}
