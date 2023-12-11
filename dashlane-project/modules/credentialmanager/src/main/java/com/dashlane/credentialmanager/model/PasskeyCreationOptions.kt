package com.dashlane.credentialmanager.model

import com.google.gson.annotations.SerializedName

data class PasskeyCreationOptions(
    @SerializedName("rp") val rp: RpEntity,
    @SerializedName("user") val user: UserEntity,
    @SerializedName("challenge") val challenge: String,
    @SerializedName("pubKeyCredParams") val pubKeyCredParams: List<CredentialParameters>,
    @SerializedName("timeout") val timeout: Long,
    @SerializedName("excludeCredentials") val excludeCredentials: List<CredentialDescriptor>,
    @SerializedName("authenticatorSelection") val authenticatorSelection: AuthenticatorSelectionCriteria,
    @SerializedName("attestation") val attestation: String
) {
    data class RpEntity(
        @SerializedName("id") val id: String,
        @SerializedName("name") val name: String
    )

    data class UserEntity(
        @SerializedName("id") val id: String,
        @SerializedName("displayName") val displayName: String
    )

    data class CredentialParameters(
        @SerializedName("type") val type: String,
        @SerializedName("alg") val alg: Long
    )

    data class CredentialDescriptor(
        @SerializedName("id") val id: String,
        @SerializedName("type") val type: String,
        @SerializedName("transports") val transports: List<String>
    )

    data class AuthenticatorSelectionCriteria(
        @SerializedName("authenticatorAttachment") val authenticatorAttachment: String,
        @SerializedName("residentKey") val residentKey: String,
        @SerializedName("requireResidentKey") val requireResidentKey: Boolean,
        @SerializedName("userVerification") val userVerification: String
    )
}