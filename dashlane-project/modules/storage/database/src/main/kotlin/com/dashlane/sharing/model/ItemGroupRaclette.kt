package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemGroupRaclette(
    @Json(name = "groupId")
    val groupId: String,
    @Json(name = "revision")
    val revision: Int,
    @Json(name = "users")
    val users: List<UserDownloadRaclette>?,
    @Json(name = "groups")
    val groups: List<UserGroupMemberRaclette>?,
    @Json(name = "collections")
    val collections: List<CollectionDownloadRaclette>? = null,
    @Json(name = "items")
    val items: List<ItemKeyRaclette>,
)