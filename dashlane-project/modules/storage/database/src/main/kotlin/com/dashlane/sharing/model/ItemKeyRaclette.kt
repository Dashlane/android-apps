package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemKeyRaclette(
    @Json(name = "itemId")
    val itemId: String,
    @Json(name = "itemKey")
    val itemKey: String,
)