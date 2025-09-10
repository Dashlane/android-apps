package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemContentRaclette(
    @Json(name = "itemId")
    val itemId: String,
    @Json(name = "timestamp")
    val timestamp: Long,
    @Json(name = "content")
    val extraData: String,
    @Json(name = "itemKeyBase64")
    val itemKeyBase64: String,
)