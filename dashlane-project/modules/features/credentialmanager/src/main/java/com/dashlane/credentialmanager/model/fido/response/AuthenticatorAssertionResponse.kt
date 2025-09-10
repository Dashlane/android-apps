package com.dashlane.credentialmanager.model.fido.response

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AuthenticatorAssertionResponse(
    @EncodeDefault @SerialName("clientDataJSON")
    val b64ClientDataJson: String? = null,
    @SerialName("authenticatorData")
    val authenticatorData: String,
    @SerialName("signature")
    val signature: String,
    @SerialName("userHandle")
    val userHandle: String,
) : AuthenticatorResponse