package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SharingUserGroups(
    @Json(name = "data")
    val userGroups: List<UserGroupRaclette>,
)

internal fun List<UserGroupRaclette>.toSharingUserGroups() = SharingUserGroups(this)