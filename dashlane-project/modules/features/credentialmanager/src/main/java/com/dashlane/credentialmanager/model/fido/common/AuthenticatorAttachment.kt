package com.dashlane.credentialmanager.model.fido.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
enum class AuthenticatorAttachment(val key: String) {
    @SerialName("platform")
    PLATFORM("platform"),

    @SerialName("cross-platform")
    CROSS_PLATFORM("cross-platform"),
    ;

    companion object {
        fun fromStringOrDefault(attachmentString: String): AuthenticatorAttachment {
            return entries.firstOrNull { it.key == attachmentString } ?: PLATFORM
        }
    }
}