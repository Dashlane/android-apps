package com.dashlane.credentialmanager.model.fido

import com.dashlane.credentialmanager.model.fido.common.AuthenticatorAttachment
import com.dashlane.credentialmanager.model.fido.common.ClientExtensionResults
import com.dashlane.credentialmanager.model.fido.common.CredentialType
import com.dashlane.credentialmanager.model.fido.response.AuthenticatorResponse
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class FidoPublicKeyCredential(
    @SerialName("id")
    val id: String,
    @SerialName("rawId")
    val rawId: String,
    @EncodeDefault @SerialName("type")
    val type: CredentialType = CredentialType.PUBLIC_KEY,
    @SerialName("authenticatorAttachment")
    val authenticatorAttachment: AuthenticatorAttachment?,
    @SerialName("response")
    val response: AuthenticatorResponse,
    @EncodeDefault @SerialName("clientExtensionResults")
    val clientExtensionResults: ClientExtensionResults = ClientExtensionResults(),
)