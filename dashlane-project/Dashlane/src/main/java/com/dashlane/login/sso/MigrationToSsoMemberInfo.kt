package com.dashlane.login.sso

import android.os.Parcelable
import com.dashlane.authentication.login.SsoInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class MigrationToSsoMemberInfo(
    val login: String,
    val serviceProviderUrl: String,
    val isNitroProvider: Boolean,
    val totpAuthTicket: String? = null
) : Parcelable

fun SsoInfo.toMigrationToSsoMemberInfo(): MigrationToSsoMemberInfo? =
    takeIf { migration == SsoInfo.Migration.TO_SSO_MEMBER }
        ?.let {
            MigrationToSsoMemberInfo(
                login = login,
                serviceProviderUrl = serviceProviderUrl,
                isNitroProvider = isNitroProvider
            )
        }