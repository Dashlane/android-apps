package com.dashlane.credentialmanager.model.fido.request

import com.dashlane.credentialmanager.model.fido.AuthenticatorFlags
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.security.MessageDigest

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class PasskeyRequestOptions(
    @SerialName("challenge")
    val challenge: String,
    @SerialName("allowCredentials")
    val allowCredentials: List<AllowCredentials>? = null,
    @SerialName("timeout")
    val timeout: Long? = null,
    @SerialName("userVerification")
    val userVerification: String,
    @SerialName("rpId")
    val rpId: String,
) {
    fun getDefaultAuthenticatorData(): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        val rpHash = md.digest(rpId.toByteArray())
        val flags = AuthenticatorFlags.USER_PRESENT or
            AuthenticatorFlags.USER_VERIFIED or
            AuthenticatorFlags.BACKUP_ELIGIBILITY or
            AuthenticatorFlags.BACKUP_STATE
        return rpHash + byteArrayOf(flags.toByte()) + byteArrayOf(0, 0, 0, 0)
    }

    @Serializable
    @JsonIgnoreUnknownKeys
    data class AllowCredentials(
        @SerialName("id")
        val id: String,
        @SerialName("transports")
        val transports: List<String>,
        @SerialName("type")
        val type: String,
    )
}