package com.dashlane.database

import com.dashlane.vault.summary.DatabaseSyncSummary

interface SyncSummaryRepository {
    suspend fun save(databaseSyncSummary: DatabaseSyncSummary)

    suspend fun load(): DatabaseSyncSummary?
}