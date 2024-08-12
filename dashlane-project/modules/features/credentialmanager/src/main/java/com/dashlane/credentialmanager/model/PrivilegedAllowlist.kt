package com.dashlane.credentialmanager.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrivilegedAllowlist(
    @Json(name = "apps") val apps: List<PrivilegedAllowlistType>
) {
    @JsonClass(generateAdapter = true)
    data class PrivilegedAllowlistType(
        @Json(name = "type") val type: String,
        @Json(name = "info") val info: PrivilegedAllowlistApp
    )

    @JsonClass(generateAdapter = true)
    data class PrivilegedAllowlistApp(
        @Json(name = "package_name") val packageName: String,
        @Json(name = "signatures") val signatures: List<PrivilegedAllowlistSignature>
    )

    @JsonClass(generateAdapter = true)
    data class PrivilegedAllowlistSignature(
        @Json(name = "build") val build: String,
        @Json(name = "cert_fingerprint_sha256") val sha256: String
    )
}
