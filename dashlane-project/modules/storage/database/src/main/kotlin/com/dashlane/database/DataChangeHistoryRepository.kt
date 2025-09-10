package com.dashlane.database

import com.dashlane.vault.summary.DatabaseDataChangeHistorySummary

interface DataChangeHistoryRepository {
    fun save(summary: DatabaseDataChangeHistorySummary)

    suspend fun load(): DatabaseDataChangeHistorySummary
}