package com.dashlane.teamspaces.db

import androidx.annotation.WorkerThread
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.counterFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoRestrictionSpaceFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationLogger.UsageLogCode11Wrapper
import com.dashlane.teamspaces.manager.SpaceDeletedNotifier
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.TeamspaceMatcher
import com.dashlane.teamspaces.manager.matchForceDomains
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.getUsageLogNameFromType
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class TeamspaceForceCategorizationManager @Inject constructor(
    @GlobalCoroutineScope
    coroutineScope: CoroutineScope,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val mainDataAccessor: MainDataAccessor,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val spaceDeletedNotifier: SpaceDeletedNotifier,
    private val teamspaceForceDeletionSharingWorker: TeamspaceForceDeletionSharingWorker
) {
    private val dataSaver: DataSaver
        get() = mainDataAccessor.getDataSaver()
    private val dataCounter: DataCounter
        get() = mainDataAccessor.getDataCounter()
    private val vaultDataQuery: VaultDataQuery
        get() = mainDataAccessor.getVaultDataQuery()
    private val genericDataQuery: GenericDataQuery
        get() = mainDataAccessor.getGenericDataQuery()

    
    @Suppress("EXPERIMENTAL_API_USAGE")
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

        val dataCount: Map<SyncObjectType, Int> = getDataCount()

        sessionManager.session?.apply {
            sendUsageLog11ForcedItems(this, result.itemsForced, teamspaces, dataCount)
            sendUsageLog11DeletedItems(this, result.itemsDeleted, teamspaces, dataCount)
        }

        teamspaceForceDeletionSharingWorker.revokeAll(result.idsSharedToRevoked)

        markNotifyServerContentDeletedIfRequire(teamspaces)
    }

    private suspend fun moveVaultItems(teamspaces: List<Teamspace?>): Result {
        val itemsForced: MutableList<VaultItem<SyncObject>> = mutableListOf()
        val itemsDeleted: MutableList<VaultItem<SyncObject>> = mutableListOf()
        val idsSharedToRevoked: MutableList<String> = mutableListOf()
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

    private suspend fun moveVaultItemsForTeamspace(
        teamspace: Teamspace,
        itemsForced: MutableList<VaultItem<SyncObject>>,
        itemsDeleted: MutableList<VaultItem<SyncObject>>,
        idsSharedToRevoked: MutableList<String>
    ) {
        val teamId: String = teamspace.teamId
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

    private fun sendUsageLog11ForcedItems(
        session: Session,
        itemsForced: List<VaultItem<SyncObject>>,
        spaces: List<Teamspace>,
        dataCount: Map<SyncObjectType, Int>
    ) {
        sendUsageLog11(session, itemsForced, spaces, false, dataCount)
    }

    private fun sendUsageLog11DeletedItems(
        session: Session,
        itemsDeleted: List<VaultItem<SyncObject>>,
        spaces: List<Teamspace>,
        dataCount: Map<SyncObjectType, Int>
    ) {
        sendUsageLog11(session, itemsDeleted, spaces, true, dataCount)
    }

    @Suppress("UNCHECKED_CAST")
    private fun sendUsageLog11(
        session: Session,
        items: List<VaultItem<SyncObject>>,
        spaces: List<Teamspace>,
        isDeleted: Boolean,
        dataCount: Map<SyncObjectType, Int>
    ) {
        val userDataRepository = bySessionUsageLogRepository[session] ?: return
        val logger = TeamspaceForceCategorizationLogger(userDataRepository)

        items.forEach { item ->
            val annonSpaceId = spaces.find { item.syncObject.spaceId == it.teamId }?.anonTeamId
            val logCode11: UsageLogCode11Wrapper = logger.newLog()
                .setSpaceId(annonSpaceId)
                .setType(getUsageLogNameFromType(item.syncObjectType))
                .setItemId(item.anonymousId)
                .setFrom(
                    if (isDeleted) UsageLogCode11.From.REMOTE_DELETE.code
                    else UsageLogCode11.From.FORCED_CATEGORIZATION.code
                )
                .setAction(
                    if (isDeleted) UsageLogCode11.Action.REMOVE
                    else UsageLogCode11.Action.EDIT
                )
                .setCounter(
                    if (isDeleted) dataCount[item.syncObjectType] 
                    else null
                )

            if (item.syncObjectType == SyncObjectType.AUTHENTIFIANT) {
                logCode11.setWebsite((item as VaultItem<SyncObject.Authentifiant>).syncObject.urlForUsageLog)
            }
            logCode11.send()
        }
    }

    private fun getUidsSharedItemForceCategorization(itemsShouldForced: List<SummaryObject>): List<String> {
        return itemsShouldForced.mapNotNull {
            if (it.isShared) {
                it.id
            } else null
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

    private fun getDataCount(): Map<SyncObjectType, Int> {
        val map: MutableMap<SyncObjectType, Int> = mutableMapOf()
        val dataTypes = TeamspaceMatcher.DATA_TYPE_TO_MATCH
        dataTypes.forEach {
            val filter = counterFilter {
                specificDataType(it)
                noSpaceFilter()
                ignoreUserLock()
            }
            map[it] = dataCounter.count(filter)
        }
        return map
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
