package com.dashlane.database

import com.dashlane.vault.summary.DatabaseDataChangeHistorySummary
import com.dashlane.vault.summary.DatabaseSummary
import com.dashlane.vault.summary.DatabaseSyncSummary

internal interface MemorySummaryRepositoryMutable : MemorySummaryRepository {
    suspend fun init()

    fun replace(newDatabaseSummary: DatabaseSummary)

    fun replace(newDatabaseSyncSummary: DatabaseSyncSummary)

    fun replace(newDatabaseDataChangeHistorySummary: DatabaseDataChangeHistorySummary)
}

internal class MemorySummaryRepositoryImpl(
    private val fileSummaryRepository: FileSummaryRepository,
    private val syncSummaryRepository: SyncSummaryRepository,
    private val dataChangeHistoryRepository: DataChangeHistoryRepository,
) : MemorySummaryRepositoryMutable {
    override var databaseSummary: DatabaseSummary? = null
        private set

    override var databaseSyncSummary: DatabaseSyncSummary? = null
        private set

    override var databaseDataChangeHistorySummary: DatabaseDataChangeHistorySummary? = null
        private set

    override suspend fun init() {
        databaseSummary = fileSummaryRepository.load()
        databaseSyncSummary = syncSummaryRepository.load()
        databaseDataChangeHistorySummary = dataChangeHistoryRepository.load()
    }

    override fun replace(newDatabaseSummary: DatabaseSummary) {
        databaseSummary = newDatabaseSummary
    }

    override fun replace(newDatabaseSyncSummary: DatabaseSyncSummary) {
        databaseSyncSummary = newDatabaseSyncSummary
    }

    override fun replace(newDatabaseDataChangeHistorySummary: DatabaseDataChangeHistorySummary) {
        databaseDataChangeHistorySummary = newDatabaseDataChangeHistorySummary
    }
}