package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserGroupMemberRaclette(
    @Json(name = "groupId")
    val groupId: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "status")
    val status: String,
    @Json(name = "permission")
    val permission: String,
    @Json(name = "proposeSignature")
    val proposeSignature: String?,
    @Json(name = "acceptSignature")
    val acceptSignature: String?,
    @Json(name = "groupKey")
    val groupKey: String?,
    @Json(name = "referrer")
    val referrer: String?,
)