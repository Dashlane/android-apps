package com.dashlane.database

import com.dashlane.database.adapter.CountryAdapter
import com.dashlane.database.adapter.InstantAdapter
import com.dashlane.database.adapter.LocalDateAdapter
import com.dashlane.database.adapter.MonthAdapter
import com.dashlane.database.adapter.SummaryObjectAdapterFactory
import com.dashlane.database.adapter.YearAdapter
import com.dashlane.vault.summary.DatabaseSummary
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okio.buffer

internal class FileSummaryRepositoryImpl(
    private val files: DatabaseFiles,
    private val reader: DatabaseReader,
    private val writer: DatabaseWriter,
) : FileSummaryRepository {
    private val jsonAdapter: JsonAdapter<DatabaseSummary> =
        Moshi.Builder()
            .add(MonthAdapter)
            .add(YearAdapter)
            .add(InstantAdapter)
            .add(LocalDateAdapter)
            .add(CountryAdapter)
            .add(SummaryObjectAdapterFactory.factory)
            .build()
            .adapter(DatabaseSummary::class.java)
            .nullSafe()

    override suspend fun save(databaseSummary: DatabaseSummary) {
        val file = files.transactionContent.summaryFile

        writer.write(file) { sink ->
            jsonAdapter.toJson(sink, databaseSummary)
        }
    }

    override suspend fun load(): DatabaseSummary? {
        val file = files.content.summaryFile
        return reader.read(file) { source ->
            jsonAdapter.fromJson(source.buffer())
        }
    }
}