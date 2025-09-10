package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SharingItemContents(
    @Json(name = "data")
    val itemContents: List<ItemContentRaclette>,
)

internal fun List<ItemContentRaclette>.toSharingItemContents() = SharingItemContents(this)