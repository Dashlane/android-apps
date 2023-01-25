package com.dashlane.storage.userdata.accessor

import com.dashlane.database.Database
import com.dashlane.database.MemorySummaryRepository
import com.dashlane.lock.LockHelper
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.userdata.accessor.filter.BaseFilter
import com.dashlane.storage.userdata.accessor.filter.FilterToPredicateSummary
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.DataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.sharing.SharingFilter
import com.dashlane.storage.userdata.accessor.filter.status.StatusFilter
import com.dashlane.storage.userdata.accessor.filter.uid.UidFilter
import com.dashlane.util.model.UserPermission
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.xml.domain.SyncObjectType
import dagger.Lazy
import javax.inject.Inject

class GenericDataQueryImplRaclette @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val filterToPredicate: FilterToPredicateSummary,
    private val lockHelper: Lazy<LockHelper>
) : GenericDataQuery {

    private val database: Database?
        get() = sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }

    private val memorySummaryRepository: MemorySummaryRepository?
        get() = database?.memorySummaryRepository

    override fun queryFirst(filter: BaseFilter): SummaryObject? {
        return queryAll(filter).firstOrNull()
    }

    override fun queryAll(filter: BaseFilter): List<SummaryObject> {
        if (lockHelper.get().forbidDataAccess(filter)) return emptyList()
        val memorySummaryRepository = memorySummaryRepository ?: return emptyList()
        val databaseSummary = memorySummaryRepository.databaseSummary?.all
        val databaseDataChangeHistorySummary =
            memorySummaryRepository.databaseDataChangeHistorySummary?.data
        if (databaseSummary == null && databaseDataChangeHistorySummary == null) {
            return emptyList()
        }
        return when {
            filter.hasDataChangeHistoryAndOthers() -> {
                val summaryResult = databaseSummary?.query(filter) ?: emptyList()
                val dataChangeHistoryResult = databaseDataChangeHistorySummary?.query(filter) ?: emptyList()
                summaryResult + dataChangeHistoryResult
            }
            filter.hasOnlyDataChangeHistory() -> {
                databaseDataChangeHistorySummary?.query(filter)
            }
            else -> {
                databaseSummary?.query(filter)
            }
        } ?: emptyList()
    }

    override fun count(filter: BaseFilter): Int {
        return queryAll(filter).size
    }

    override fun createFilter(): GenericFilter = GenericFilter()

    private fun List<SummaryObject>.query(filter: BaseFilter): List<SummaryObject> =
        asSequence().applyAllFilters(filter).toList()

    private fun Sequence<SummaryObject>.applyAllFilters(filter: BaseFilter): Sequence<SummaryObject> =
        filterId(filter)
            .filterDataType(filter)
            .filterStatus(filter)
            .filterSharingPermission(filter)
            .filter(filterToPredicate.toPredicate(filter))

    private fun Sequence<SummaryObject>.filterId(filter: BaseFilter): Sequence<SummaryObject> {
        if (filter !is UidFilter) return this
        return filter {
            filter.onlyOnUids ?: return@filter true
            it.id in filter.onlyOnUids!!
        }
    }

    private fun Sequence<SummaryObject>.filterDataType(filter: DataTypeFilter) =
        filter {
            if (it.syncObjectType in SKIP_DATA_TYPES) return@filter false
            filter.has(it.syncObjectType)
        }

    private fun Sequence<SummaryObject>.filterStatus(filter: StatusFilter) =
        filter { summaryObject ->
            if (filter.onlyVisibleStatus) {
                summaryObject.syncState == SyncState.IN_SYNC_MODIFIED ||
                        summaryObject.syncState == SyncState.MODIFIED ||
                        summaryObject.syncState == SyncState.SYNCED
            } else {
                true
            }
        }

    private fun Sequence<SummaryObject>.filterSharingPermission(filter: BaseFilter): Sequence<SummaryObject> {
        if (filter !is SharingFilter) return this

        return filter { summaryObject ->
            if (summaryObject.syncObjectType !in SyncObjectTypeUtils.SHAREABLE) return@filter true
            
            filter.sharingPermissions?.any {
                
                (it == UserPermission.UNDEFINED && summaryObject.sharingPermission == null) ||
                        (summaryObject.sharingPermission == it)
            } ?: true
        }
    }

    private fun DataTypeFilter.hasOnlyDataChangeHistory() =
        has(SyncObjectType.DATA_CHANGE_HISTORY) && this.dataTypes.size == 1

    private fun DataTypeFilter.hasDataChangeHistoryAndOthers() =
        has(SyncObjectType.DATA_CHANGE_HISTORY) && this.dataTypes.size > 1

    companion object {
        private val SKIP_DATA_TYPES = arrayOf(SyncObjectType.SETTINGS)
    }
}