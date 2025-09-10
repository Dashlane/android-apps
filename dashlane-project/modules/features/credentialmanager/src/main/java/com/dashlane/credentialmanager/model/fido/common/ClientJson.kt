package com.dashlane.credentialmanager.model.fido.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ClientJson(
    @SerialName("type")
    val type: RequestType,
    @SerialName("challenge")
    val challenge: String,
    @SerialName("origin")
    val origin: String,
    @SerialName("androidPackageName")
    val androidPackageName: String,
)