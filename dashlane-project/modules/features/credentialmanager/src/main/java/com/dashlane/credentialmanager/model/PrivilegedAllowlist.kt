package com.dashlane.credentialmanager.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivilegedAllowlist(
    @SerialName("apps") val apps: List<PrivilegedAllowlistType>,
) {
    @Serializable
    data class PrivilegedAllowlistType(
        @SerialName("type") val type: String,
        @SerialName("info") val info: PrivilegedAllowlistApp,
    )

    @Serializable
    data class PrivilegedAllowlistApp(
        @SerialName("package_name") val packageName: String,
        @SerialName("signatures") val signatures: List<PrivilegedAllowlistSignature>,
    )

    @Serializable
    data class PrivilegedAllowlistSignature(
        @SerialName("build") val build: String,
        @SerialName("cert_fingerprint_sha256") val sha256: String,
    )
}