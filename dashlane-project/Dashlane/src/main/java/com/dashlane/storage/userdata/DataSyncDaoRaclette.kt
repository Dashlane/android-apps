package com.dashlane.storage.userdata

import com.dashlane.database.BackupRepository
import com.dashlane.database.Id
import com.dashlane.database.MemorySummaryRepository
import com.dashlane.database.VaultObjectRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.sync.VaultItemBackupWrapper
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.DatabaseSyncSummary
import com.dashlane.vault.summary.SyncSummaryItem
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import javax.inject.Inject

typealias PendingSyncItemRaclette = Pair<VaultItemBackupWrapper<SyncObject>, SyncState>

class DataSyncDaoRaclette @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val racletteLogger: RacletteLogger
) {
    private val memorySummaryRepository: MemorySummaryRepository?
        get() {
            val database =
                sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }
            return database?.memorySummaryRepository
        }
    private val vaultObjectRepository: VaultObjectRepository?
        get() {
            val database =
                sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }
            return database?.vaultObjectRepository
        }
    private val backupRepository: BackupRepository?
        get() {
            val database =
                sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }
            return database?.backupRepository
        }

    suspend fun applyBackupDate(uuid: List<String>, backupTime: Instant) {
        val vaultObjectRepository = vaultObjectRepository ?: return
        runCatching {
            val itemsToSync = uuid.mapNotNull {
                val vaultItem: VaultItem<*> =
                    vaultObjectRepository[Id.of(it)] ?: return@mapNotNull null
                vaultItem.copy(
                    backupDate = backupTime
                )
            }
            vaultObjectRepository.transaction {
                itemsToSync.forEach {
                    update(it)
                }
            }
        }.onFailure { racletteLogger.exception(it) }.getOrThrow()
    }

    suspend fun markItemsInSync(dataTypes: List<SyncObjectType>) {
        markItemsInSync(dataTypes, SyncState.IN_SYNC_MODIFIED, SyncState.MODIFIED)
        markItemsInSync(dataTypes, SyncState.IN_SYNC_DELETED, SyncState.DELETED)
    }

    fun getItemWithExtraData(id: String): VaultItemBackupWrapper<SyncObject>? {
        return getItemsWithExtraData(listOf(id)).firstOrNull()?.first
    }

    fun getItemsWithExtraData(ids: List<String>): List<PendingSyncItemRaclette> {
        val vaultObjectRepository = vaultObjectRepository ?: return emptyList()
        return ids.mapNotNull {
            val id = Id.of(it)
            val vaultItem = vaultObjectRepository[id] ?: return@mapNotNull null
            val backup = backupRepository?.load(id)
            VaultItemBackupWrapper(
                vaultItem = vaultItem,
                backup = backup
            ) to vaultItem.syncState
        }
    }

    fun getItemsPendingSyncForType(): List<PendingSyncItemRaclette> {
        val vaultObjectRepository = vaultObjectRepository ?: return emptyList()
        val databaseSyncSummary = memorySummaryRepository?.databaseSyncSummary ?: return emptyList()
        val ids = databaseSyncSummary.items.filter {
            it.syncState == SyncState.IN_SYNC_MODIFIED || it.syncState == SyncState.IN_SYNC_DELETED
        }.map { it.id }

        return ids.mapNotNull {
            val id = Id.of(it)
            val vaultItem = vaultObjectRepository[id] ?: return@mapNotNull null
            val backup = backupRepository?.load(id)
            VaultItemBackupWrapper(
                vaultItem = vaultItem,
                backup = backup
            ) to vaultItem.syncState
        }
    }

    suspend fun markAllItemsAsSynced() {
        val vaultObjectRepository = vaultObjectRepository ?: return
        val databaseSyncSummary = memorySummaryRepository?.databaseSyncSummary ?: return
        val idsToDelete = databaseSyncSummary.items.filter {
            it.type in SyncObjectTypeUtils.ALL && it.syncState == SyncState.IN_SYNC_DELETED
        }
        runCatching {
            val itemsToSync = databaseSyncSummary.items.filter {
                it.type in SyncObjectTypeUtils.ALL && it.syncState == SyncState.IN_SYNC_MODIFIED
            }.mapNotNull {
                vaultObjectRepository[Id.of(it.id)]?.copyWithAttrs {
                    syncState = SyncState.SYNCED
                }
            }
            vaultObjectRepository.transaction {
                idsToDelete.forEach {
                    delete(Id.of(it.id))
                }
                itemsToSync.forEach {
                    update(it)
                }
            }
        }.onFailure { racletteLogger.exception(it) }.getOrThrow()
    }

    suspend fun markItemsInSync(
        dataTypes: List<SyncObjectType>,
        syncedState: SyncState,
        currentState: SyncState
    ) {
        val databaseSyncSummary = memorySummaryRepository?.databaseSyncSummary ?: return
        val ids = databaseSyncSummary.items.filter {
            it.type in dataTypes && it.syncState == currentState
        }.map { it.id }
        val vaultObjectRepository = vaultObjectRepository ?: return
        runCatching {
            val vaultItems = ids.mapNotNull {
                val vaultItem = vaultObjectRepository[Id.of(it)]
                vaultItem?.copyWithAttrs { syncState = syncedState }
            }
            vaultObjectRepository.transaction {
                vaultItems.forEach { update(it) }
            }
        }.onFailure { racletteLogger.exception(it) }.getOrThrow()
    }

    suspend fun markItemSyncState(uid: String, syncedState: SyncState) {
        val vaultObjectRepository = vaultObjectRepository ?: return
        runCatching {
            val vaultItem = vaultObjectRepository[Id.of(uid)]
            vaultItem?.copyWithAttrs { syncState = syncedState }?.also {
                vaultObjectRepository.transaction {
                    update(it)
                }
            }
        }.onFailure { racletteLogger.exception(it) }.getOrThrow()
    }

    fun getItemsForSummary(): List<Triple<String, SyncObjectType, Instant?>> {
        val databaseSyncSummary = memorySummaryRepository?.databaseSyncSummary ?: return emptyList()
        return databaseSyncSummary.items.map {
            Triple(it.id, it.type, it.backupDate)
        }
    }

    fun getDuplicationCandidates(types: List<SyncObjectType>): List<List<PendingSyncItemRaclette>> {
        val databaseSyncSummary = memorySummaryRepository?.databaseSyncSummary ?: return emptyList()
        val vaultObjectRepository = vaultObjectRepository ?: return emptyList()
        return types.flatMap { getDuplicationCandidates(it, databaseSyncSummary, vaultObjectRepository) }
    }

    private fun getDuplicationCandidates(
        type: SyncObjectType,
        databaseSyncSummary: DatabaseSyncSummary,
        vaultObjectRepository: VaultObjectRepository
    ): List<List<PendingSyncItemRaclette>> {
        val list: List<List<SyncSummaryItem>> =
            databaseSyncSummary.items
                .filter { type == it.type }
                .groupBy { it.userContentData }
                .values
                .filter { it.size > 1 }
                .toList()
        return list.map {
            it.mapNotNull { item ->
                val id = Id.of(item.id)
                val vaultItem = vaultObjectRepository[id] ?: return@mapNotNull null
                val backup = backupRepository?.load(id)
                VaultItemBackupWrapper(
                    vaultItem = vaultItem,
                    backup = backup
                ) to vaultItem.syncState
            }
        }
    }
}
