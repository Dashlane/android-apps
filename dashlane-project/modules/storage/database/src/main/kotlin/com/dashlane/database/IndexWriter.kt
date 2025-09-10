package com.dashlane.database

import com.dashlane.database.DatabaseIndex.Companion.SUMMARY_VERSION
import com.dashlane.database.DatabaseIndex.Companion.VERSION
import com.squareup.moshi.JsonAdapter

internal class IndexWriter(
    private val files: DatabaseFiles,
    private val writer: DatabaseWriter,
) {
    private val jsonAdapter: JsonAdapter<DatabaseIndex> =
        DatabaseIndex.JSON_ADAPTER

    @Throws(DatabaseIndexInitializationException::class)
    fun init(user: String): DatabaseIndex {
        return DatabaseIndex(
            version = VERSION,
            summaryVersion = SUMMARY_VERSION,
            user = user,
        ).also {
            save(it)
        }
    }

    fun save(index: DatabaseIndex) {
        val file = files.index
        try {
            writer.write(file) { sink ->
                jsonAdapter.toJson(sink, index)
            }
        } catch (e: Exception) {
            throw DatabaseIndexInitializationException(cause = e)
        }
    }
}