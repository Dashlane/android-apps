package com.dashlane.database

import com.dashlane.database.adapter.CountryAdapter
import com.dashlane.database.adapter.InstantAdapter
import com.dashlane.database.adapter.LocalDateAdapter
import com.dashlane.vault.summary.DatabaseSyncSummary
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.buffer

internal class SyncSummaryRepositoryImpl(
    private val files: DatabaseFiles,
    private val reader: DatabaseReader,
    private val writer: DatabaseWriter,
) : SyncSummaryRepository {
    private val jsonAdapter: JsonAdapter<DatabaseSyncSummary> =
        Moshi.Builder()
            .add(InstantAdapter)
            .add(LocalDateAdapter)
            .add(CountryAdapter)
            .build()
            .adapter(DatabaseSyncSummary::class.java)
            .nullSafe()

    override suspend fun save(databaseSyncSummary: DatabaseSyncSummary) {
        val file = files.transactionContent.syncSummaryFile
        writer.write(file) { sink ->
            jsonAdapter.toJson(sink, databaseSyncSummary)
        }
    }

    override suspend fun load(): DatabaseSyncSummary? {
        val file = files.content.syncSummaryFile
        return reader.read(file) { source ->
            jsonAdapter.fromJson(source.buffer())
        }
    }
}