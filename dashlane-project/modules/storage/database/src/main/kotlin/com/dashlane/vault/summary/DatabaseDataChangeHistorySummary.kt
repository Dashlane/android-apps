package com.dashlane.vault.summary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatabaseDataChangeHistorySummary(
    @Json(name = FIELD_DATA)
    val data: List<SummaryObject.DataChangeHistory>,
) {
    companion object {
        private const val FIELD_DATA = "data"
    }
}

internal fun List<SummaryObject.DataChangeHistory>.toDatabaseDataChangeHistorySummary() =
    DatabaseDataChangeHistorySummary(this)