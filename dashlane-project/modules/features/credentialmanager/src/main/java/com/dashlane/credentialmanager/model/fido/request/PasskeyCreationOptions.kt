package com.dashlane.credentialmanager.model.fido.request

import com.dashlane.credentialmanager.model.fido.common.AuthenticatorSelectionCriteria
import com.dashlane.credentialmanager.model.fido.common.CredentialType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class PasskeyCreationOptions(
    @SerialName("rp")
    val rp: RpEntity,
    @SerialName("user")
    val user: UserEntity,
    @SerialName("challenge")
    val challenge: String,
    @SerialName("pubKeyCredParams")
    val pubKeyCredParams: List<CredentialParameters>,
    @SerialName("timeout")
    val timeout: Long? = null,
    @SerialName("excludeCredentials")
    val excludeCredentials: List<CredentialDescriptor>?,
    @SerialName("authenticatorSelection")
    val authenticatorSelection: AuthenticatorSelectionCriteria,
    @SerialName("attestation")
    val attestation: String,
) {
    @Serializable
    @JsonIgnoreUnknownKeys
    data class RpEntity(
        @SerialName("id")
        val id: String,
        @SerialName("name")
        val name: String,
    )

    @Serializable
    @JsonIgnoreUnknownKeys
    data class UserEntity(
        @SerialName("id")
        val id: String,
        @SerialName("displayName")
        val displayName: String,
    )

    @Serializable
    @JsonIgnoreUnknownKeys
    data class CredentialParameters(
        @SerialName("type")
        val type: String,
        @SerialName("alg")
        val alg: Long,
    )

    @Serializable
    @JsonIgnoreUnknownKeys
    data class CredentialDescriptor(
        @SerialName("id")
        val id: String,
        @SerialName("type")
        val type: CredentialType,
        @SerialName("transports")
        val transports: List<String>,
    )
}