package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SharingCollections(
    @Json(name = "data")
    val collections: List<CollectionRaclette>,
)

internal fun List<CollectionRaclette>.toSharingCollections() = SharingCollections(this)