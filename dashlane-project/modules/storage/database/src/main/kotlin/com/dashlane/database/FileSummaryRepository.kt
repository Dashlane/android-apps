package com.dashlane.database

import com.dashlane.vault.summary.DatabaseSummary

interface FileSummaryRepository {
    suspend fun save(databaseSummary: DatabaseSummary)

    suspend fun load(): DatabaseSummary?
}