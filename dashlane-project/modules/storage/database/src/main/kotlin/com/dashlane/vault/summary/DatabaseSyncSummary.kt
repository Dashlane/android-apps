package com.dashlane.vault.summary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatabaseSyncSummary(
    @Json(name = FIELD_DATA)
    val items: List<SyncSummaryItem>,
) {
    companion object {
        private const val FIELD_DATA = "data"
    }
}

internal fun List<SyncSummaryItem>.toDatabaseSyncSummary() = DatabaseSyncSummary(this)