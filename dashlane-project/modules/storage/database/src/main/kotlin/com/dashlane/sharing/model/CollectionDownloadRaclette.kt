package com.dashlane.sharing.model
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionDownloadRaclette(
    @Json(name = "itemGroupKey")
    val itemGroupKey: String? = null,
    @Json(name = "referrer")
    val referrer: String? = null,
    @Json(name = "name")
    val name: String,
    @Json(name = "permission")
    val permission: String,
    @Json(name = "proposeSignature")
    val proposeSignature: String? = null,
    @Json(name = "uuid")
    val uuid: String,
    @Json(name = "status")
    val status: String,
    @Json(name = "acceptSignature")
    val acceptSignature: String? = null,
)