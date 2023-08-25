package com.dashlane.storage.userdata.accessor

import com.dashlane.database.Database
import com.dashlane.database.MemorySummaryRepository
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.filter.DataChangeHistoryFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import dagger.Lazy
import javax.inject.Inject

class DataChangeHistoryQueryRacletteImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDatabaseRepository,
    private val dataStorageProvider: Lazy<DataStorageProvider>
) : DataChangeHistoryQuery {

    private val database: Database?
        get() = sessionManager.session?.let { userDataRepository.getRacletteDatabase(it) }

    private val memorySummaryRepository: MemorySummaryRepository?
        get() = database?.memorySummaryRepository

    private val vaultDataQuery: VaultDataQuery
        get() = dataStorageProvider.get().vaultDataQuery

    @Suppress("UNCHECKED_CAST")
    override fun query(filter: DataChangeHistoryFilter): VaultItem<SyncObject.DataChangeHistory>? {
        val summary = memorySummaryRepository?.databaseDataChangeHistorySummary ?: return null
        val item: SummaryObject.DataChangeHistory = summary.data.find {
            it.objectId == filter.objectUid
        } ?: return null
        return vaultDataQuery.query(vaultFilter { specificUid(item.id) }) as? VaultItem<SyncObject.DataChangeHistory>
    }
}
