package com.dashlane.database

import com.dashlane.vault.summary.DatabaseDataChangeHistorySummary
import com.dashlane.vault.summary.DatabaseSummary
import com.dashlane.vault.summary.DatabaseSyncSummary

interface MemorySummaryRepository {
    val databaseSummary: DatabaseSummary?

    val databaseSyncSummary: DatabaseSyncSummary?

    val databaseDataChangeHistorySummary: DatabaseDataChangeHistorySummary?
}