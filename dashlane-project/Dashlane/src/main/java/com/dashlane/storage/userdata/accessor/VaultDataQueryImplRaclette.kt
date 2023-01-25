package com.dashlane.storage.userdata.accessor

import com.dashlane.database.Database
import com.dashlane.database.Id
import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.filter.FilterToPredicate
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.DataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.status.StatusFilter
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import dagger.Lazy
import javax.inject.Inject

class VaultDataQueryImplRaclette @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val filterToPredicate: FilterToPredicate,
    private val dataStorageProvider: Lazy<DataStorageProvider>,
    private val lockHelper: Lazy<LockHelper>
) : VaultDataQuery {
    private val database: Database?
        get() = sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }

    private val genericDataQuery: GenericDataQuery
        get() = dataStorageProvider.get().genericDataQuery

    override fun query(filter: VaultFilter): VaultItem<SyncObject>? {
        return queryAll(filter).firstOrNull()
    }

    override fun queryAll(filter: VaultFilter): List<VaultItem<SyncObject>> {
        if (lockHelper.get().forbidDataAccess(filter)) return listOf()
        val database = database ?: return emptyList()
        val ids = getIdsToQuery(filter)
        val vaultItems = ids.mapNotNull { id ->
            database.vaultObjectRepository[Id.of(id)]
        }.asSequence()
        return vaultItems
            .filterDataType(filter)
            .filterStatus(filter)
            .filter(filterToPredicate.toPredicate(filter))
            .toList()
    }

    private fun Sequence<VaultItem<SyncObject>>.filterDataType(filter: DataTypeFilter) =
        filter { it.syncObjectType in filter.dataTypes }

    private fun Sequence<VaultItem<SyncObject>>.filterStatus(filter: StatusFilter) =
        filter {
            if (filter.onlyVisibleStatus) {
                it.syncState == SyncState.IN_SYNC_MODIFIED ||
                        it.syncState == SyncState.MODIFIED ||
                        it.syncState == SyncState.SYNCED
            } else {
                true
            }
        }

    private fun getIdsToQuery(filter: VaultFilter): List<String> {
        return if (filter.uidFilter.onlyOnUids == null) {
            val genericFilter = genericFilter {
                uidFilter = filter.uidFilter
                spaceFilter = filter.spaceFilter
                dataTypeFilter = filter.dataTypeFilter
                lockFilter = filter.lockFilter
                statusFilter = filter.statusFilter
            }
            genericDataQuery.queryAll(genericFilter).map { it.id }
        } else {
            filter.uidFilter.onlyOnUids!!.toList()
        }
    }
}