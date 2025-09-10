package com.dashlane.vault.summary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatabaseSummary(
    @Json(name = FIELD_DATA)
    val data: List<SummaryObject>,
) {
    companion object {
        private const val FIELD_DATA = "data"
    }

    val size: Int
        get() = data.size
}