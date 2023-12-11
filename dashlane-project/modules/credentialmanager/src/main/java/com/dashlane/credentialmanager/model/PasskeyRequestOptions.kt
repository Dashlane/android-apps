package com.dashlane.credentialmanager.model

import com.google.gson.annotations.SerializedName

data class PasskeyRequestOptions(
    @SerializedName("challenge") val challenge: String,
    @SerializedName("allowCredentials") val allowCredentials: List<AllowCredentials>,
    @SerializedName("timeout") val timeout: Long,
    @SerializedName("userVerification") val userVerification: String,
    @SerializedName("rpId") val rpId: String,
) {
    data class AllowCredentials(
        @SerializedName("id") val id: String,
        @SerializedName("transports") val transports: List<String>,
        @SerializedName("type") val type: String,
    )
}