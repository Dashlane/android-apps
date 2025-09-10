package com.dashlane.database

import com.dashlane.database.adapter.CountryAdapter
import com.dashlane.database.adapter.InstantAdapter
import com.dashlane.database.adapter.LocalDateAdapter
import com.dashlane.vault.summary.DatabaseDataChangeHistorySummary
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.buffer

internal class DataChangeHistoryRepositoryImpl(
    private val files: DatabaseFiles,
    private val reader: DatabaseReader,
    private val writer: DatabaseWriter,
) : DataChangeHistoryRepository {
    private val jsonAdapter: JsonAdapter<DatabaseDataChangeHistorySummary> =
        Moshi.Builder()
            .add(InstantAdapter)
            .add(LocalDateAdapter)
            .add(CountryAdapter)
            .build()
            .adapter(DatabaseDataChangeHistorySummary::class.java)
            .nullSafe()

    override suspend fun load(): DatabaseDataChangeHistorySummary {
        val file = files.content.dataChangeHistorySummaryFile
        return reader.read(file) { source ->
            jsonAdapter.fromJson(source.buffer())
        } ?: DatabaseDataChangeHistorySummary(emptyList())
    }

    override fun save(summary: DatabaseDataChangeHistorySummary) {
        val file = files.transactionContent.dataChangeHistorySummaryFile
        writer.write(file) { sink ->
            jsonAdapter.toJson(sink, summary)
        }
    }
}