package com.dashlane.database.model

import com.dashlane.xml.domain.SyncObjectType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class SearchItem(
    @Json(name = "dataId")
    val dataId: String,
    @Json(name = "dataType")
    val dataType: SyncObjectType,
    @Json(name = "lastUsed")
    val lastUsed: Instant,
    @Json(name = "hitCount")
    val hitCount: Int,
)