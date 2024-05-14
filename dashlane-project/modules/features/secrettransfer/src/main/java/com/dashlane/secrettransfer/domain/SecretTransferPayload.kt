package com.dashlane.secrettransfer.domain

import com.dashlane.account.UserAccountInfo
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

private const val VERSION = 1

@Parcelize
@JsonClass(generateAdapter = true)
data class SecretTransferPayload(
    val login: String,
    @Json(name = "key") val vaultKey: VaultKey,
    val token: String?,
    val version: Int = VERSION
) : Parcelable {

    @Parcelize
    @JsonClass(generateAdapter = true)
    data class VaultKey(
        val type: Type,
        val value: String
    ) : Parcelable

    @JsonClass(generateAdapter = false)
    enum class Type {
        @Json(name = "master_password")
        MASTER_PASSWORD,

        @Json(name = "invisible_master_password")
        INVISIBLE_MASTER_PASSWORD,

        @Json(name = "sso")
        SSO
    }
}

fun SecretTransferPayload.Type.toUserAccountInfoType(): UserAccountInfo.AccountType {
    return when (this) {
        SecretTransferPayload.Type.SSO,
        SecretTransferPayload.Type.MASTER_PASSWORD -> UserAccountInfo.AccountType.MasterPassword

        SecretTransferPayload.Type.INVISIBLE_MASTER_PASSWORD -> UserAccountInfo.AccountType.InvisibleMasterPassword
    }
}
