package com.dashlane.sharing.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionRaclette(
    @Json(name = "privateKey")
    val privateKey: String,
    @Json(name = "userGroups")
    val userGroups: List<UserGroupCollectionDownloadRaclette>? = null,
    @Json(name = "name")
    val name: String,
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "uuid")
    val uuid: String,
    @Json(name = "users")
    val users: List<UserCollectionDownloadRaclette>? = null,
    @Json(name = "revision")
    val revision: Long,
)