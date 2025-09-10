package com.dashlane.secrettransfer.domain

import com.dashlane.user.UserAccountInfo
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val VERSION = 1

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SecretTransferPayload(
    @SerialName("login") val login: String,
    @SerialName("key") val vaultKey: VaultKey,
    @SerialName("token") val token: String?,
    @EncodeDefault @SerialName("version") val version: Int = VERSION,
) {
    @Serializable
    data class VaultKey(
        @SerialName("type") val type: Type,
        @SerialName("value") val value: String,
    )

    @Serializable
    enum class Type {
        @SerialName("master_password")
        MASTER_PASSWORD,

        @SerialName("invisible_master_password")
        INVISIBLE_MASTER_PASSWORD,

        @SerialName("sso")
        SSO,
    }
}

fun SecretTransferPayload.Type.toUserAccountInfoType(): UserAccountInfo.AccountType {
    return when (this) {
        SecretTransferPayload.Type.SSO,
        SecretTransferPayload.Type.MASTER_PASSWORD,
        -> UserAccountInfo.AccountType.MasterPassword

        SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD -> UserAccountInfo.AccountType.InvisibleMasterPassword
    }
}