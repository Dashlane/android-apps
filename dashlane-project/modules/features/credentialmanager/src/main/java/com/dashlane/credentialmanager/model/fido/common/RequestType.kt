package com.dashlane.credentialmanager.model.fido.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
enum class RequestType {
    @SerialName("webauthn.get")
    GET,

    @SerialName("webauthn.create")
    CREATE,
}