package com.dashlane.credentialmanager.model.fido.common

import com.dashlane.server.api.endpoints.passkeys.RegisterPasskeyService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
enum class CredentialType(val key: String) {
    @SerialName("public-key")
    PUBLIC_KEY("public-key"),
    ;

    companion object {
        fun fromTypeStringOrDefault(type: String): CredentialType {
            return entries.firstOrNull { it.key == type } ?: PUBLIC_KEY
        }

        fun fromResponseType(type: RegisterPasskeyService.Data.CredentialRegisterData.Type): CredentialType {
            return fromTypeStringOrDefault(type = type.key)
        }
    }
}