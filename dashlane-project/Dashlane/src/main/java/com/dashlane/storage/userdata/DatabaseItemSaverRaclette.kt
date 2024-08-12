package com.dashlane.storage.userdata

import com.dashlane.core.sharing.SharedItemMerger
import com.dashlane.database.Id
import com.dashlane.database.VaultObjectRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.VaultItemBackupWrapper
import com.dashlane.useractivity.RacletteLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.isSupportedSyncObjectType
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseItemSaverRaclette @Inject constructor(
    private val vaultDataQuery: Lazy<VaultDataQuery>,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val racletteLogger: RacletteLogger
) : DatabaseItemSaver {
    private val vaultObjectRepository: VaultObjectRepository?
        get() {
            val database =
                sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }
            return database?.vaultObjectRepository
        }

    override suspend fun delete(item: VaultItem<*>): Boolean = delete(item.uid)

    suspend fun delete(uid: String): Boolean {
        return runCatching {
            vaultObjectRepository?.transaction {
                delete(Id.of(uid))
            }
        }.onFailure {
            racletteLogger.exception(it)
        }.isSuccess
    }

    suspend fun saveItemFromSharingSync(itemWrapper: VaultItemBackupWrapper<SyncObject>): Boolean {
        val vaultObjectRepository = vaultObjectRepository ?: return false
        val itemToSave =
            getItemToSave(itemWrapper, DataSaver.SaveRequest.Origin.SHARING_SYNC) ?: return false
        return runCatching {
            vaultObjectRepository.transaction {
                update(itemToSave)
            }
        }.onFailure {
            racletteLogger.exception(it)
        }.isSuccess
    }

    override suspend fun <T : SyncObject> save(
        saveRequest: DataSaver.SaveRequest<T>,
        oldItems: List<VaultItem<*>>
    ): List<VaultItem<T>> {
        val vaultObjectRepository = vaultObjectRepository ?: return emptyList()
        val origin = saveRequest.origin
        return runCatching {
            vaultObjectRepository.transaction {
                saveRequest.itemsToSave.forEach { item ->
                    
                    if (!item.syncObject.isSupportedSyncObjectType) return@forEach
                    val oldItem = oldItems.firstOrNull { it.uid == item.uid }
                    val itemToSave = getItemToSave(
                        VaultItemBackupWrapper(vaultItem = item, backup = null),
                        oldItem,
                        origin
                    )
                        ?: return@forEach
                    update(itemToSave)
                }
            }
        }.onFailure {
            racletteLogger.exception(it)
        }.map {
            saveRequest.itemsToSave
        }.getOrDefault(emptyList())
    }

    private fun getItemFromDb(itemUid: String): VaultItem<*>? {
        val filter = vaultFilter {
            ignoreUserLock()
            specificUid(itemUid)
            allStatusFilter()
        }
        return vaultDataQuery.get().queryLegacy(filter)
    }

    suspend fun inTransaction(block: VaultObjectRepository.Transaction.() -> Unit) =
        runCatching {
            vaultObjectRepository?.transaction {
                apply(block)
            }
        }.onFailure {
            racletteLogger.exception(it)
        }.getOrNull()

    fun getItemToSave(
        itemWrapper: VaultItemBackupWrapper<SyncObject>,
        origin: DataSaver.SaveRequest.Origin
    ): VaultItemBackupWrapper<SyncObject>? {
        val item = itemWrapper.vaultItem
        if (item.syncObject is SyncObject.DataChangeHistory) {
            return itemWrapper
        }
        val itemInDb = getItemFromDb(item.uid)
        return getItemToSave(itemWrapper, itemInDb, origin)
    }

    fun getItemToSave(
        itemWrapper: VaultItemBackupWrapper<SyncObject>,
        itemInDb: VaultItem<SyncObject>?,
        origin: DataSaver.SaveRequest.Origin
    ): VaultItemBackupWrapper<SyncObject>? {
        val item = itemWrapper.vaultItem

        if (!SharedItemMerger.needSharingMerge(itemInDb, item)) {
            return itemWrapper
        }

        val mergedVaultItem =
            SharedItemMerger.mergeVaultItemToSave(itemInDb, item, origin) ?: return null
        return itemWrapper.copy(vaultItem = mergedVaultItem)
    }
}