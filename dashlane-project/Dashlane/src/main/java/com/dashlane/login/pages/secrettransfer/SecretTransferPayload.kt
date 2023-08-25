package com.dashlane.login.pages.secrettransfer

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

private const val VERSION = 1

@JsonClass(generateAdapter = true)
data class SecretTransferPayload(
    val login: String,
    @Json(name = "key") val vaultKey: VaultKey,
    val token: String?,
    val version: Int = VERSION
) {

    @JsonClass(generateAdapter = true)
    data class VaultKey(
        val type: Type,
        val value: String
    )

    @JsonClass(generateAdapter = false)
    enum class Type {
        @Json(name = "master_password")
        MASTER_PASSWORD,

        @Json(name = "sso")
        SSO
    }
}
