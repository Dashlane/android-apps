package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SharingItemGroups(
    @Json(name = "data")
    val itemGroups: List<ItemGroupRaclette>,
)

internal fun List<ItemGroupRaclette>.toSharingItemGroups() = SharingItemGroups(this)