package com.dashlane.database.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatabaseSearch(
    @Json(name = FIELD_DATA)
    val items: List<SearchItem>,
) {
    companion object {
        private const val FIELD_DATA = "data"
    }
}

internal fun List<SearchItem>.toDatabaseSearch() = DatabaseSearch(this)