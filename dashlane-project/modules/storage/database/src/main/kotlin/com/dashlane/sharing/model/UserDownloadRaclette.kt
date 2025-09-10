package com.dashlane.sharing.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDownloadRaclette(
    @Json(name = "userId")
    val userId: String,
    @Json(name = "alias")
    val alias: String,
    @Json(name = "permission")
    val permission: String,
    @Json(name = "proposeSignature")
    val proposeSignature: String? = null,
    @Json(name = "groupKey")
    val groupKey: String? = null,
    @Json(name = "acceptSignature")
    val acceptSignature: String? = null,
    @Json(name = "status")
    val status: String,
    @Json(name = "referrer")
    val referrer: String,
    @Json(name = "proposeSignatureUsingAlias")
    val proposeSignatureUsingAlias: Boolean = false,
    @Json(name = "rsaStatus")
    val rsaStatus: String,
)