package com.dashlane.credentialmanager.model.fido.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import com.dashlane.server.api.endpoints.passkeys.RegisterPasskeyService.Data.CredentialRegisterData.ClientExtensionResults as RegisterClientExtensionResults
import com.dashlane.server.api.endpoints.passkeys.UsePasskeyService.Data.CredentialGetData.ClientExtensionResults as UseClientExtensionResults

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class ClientExtensionResults(
    @SerialName("credProps")
    val credProps: CredProps? = null,
    @SerialName("appid")
    val appid: Boolean? = null,
    @SerialName("hmacCreateSecret")
    val hmacCreateSecret: Boolean? = null,
) {
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    data class CredProps(
        @SerialName("rk")
        val rk: Boolean? = null,
    )
}

internal fun RegisterClientExtensionResults?.toInternalClientExtensionResults(): ClientExtensionResults =
    ClientExtensionResults(
        credProps = this?.credProps?.let { ClientExtensionResults.CredProps(rk = it.rk) },
        appid = this?.appid,
        hmacCreateSecret = this?.hmacCreateSecret,
    )

internal fun UseClientExtensionResults?.toInternalClientExtensionResults(): ClientExtensionResults =
    ClientExtensionResults(credProps = this?.credProps?.let { ClientExtensionResults.CredProps(rk = it.rk) })