package com.dashlane.credentialmanager.model.fido.common

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class AuthenticatorSelectionCriteria(
    @EncodeDefault @SerialName("authenticatorAttachment")
    val authenticatorAttachment: AuthenticatorAttachment = AuthenticatorAttachment.PLATFORM,
    @SerialName("residentKey")
    val residentKey: String,
    @EncodeDefault @SerialName("requireResidentKey")
    val requireResidentKey: Boolean = false,
    @SerialName("userVerification")
    val userVerification: String,
)