package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserGroupRaclette(
    @Json(name = "groupId")
    val groupId: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "teamId")
    val teamId: String?,
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "privateKey")
    val privateKey: String,
    @Json(name = "revision")
    val revision: Int,
    @Json(name = "users")
    val users: List<UserDownloadRaclette>,
)