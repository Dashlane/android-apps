package com.dashlane.authentication.login

import com.dashlane.server.api.endpoints.authentication.AuthSsoInfo

data class SsoInfo(
    val serviceProviderUrl: String,
    val isNitroProvider: Boolean,
    val migration: Migration?
) {
    enum class Migration { TO_SSO_MEMBER, TO_MASTER_PASSWORD_USER }
}

internal fun AuthSsoInfo.toAuthenticationSsoInfo() = SsoInfo(
    serviceProviderUrl = serviceProviderUrl,
    isNitroProvider = isNitroProvider ?: false,
    migration = migration?.let {
        when (it) {
            AuthSsoInfo.Migration.SSO_MEMBER_TO_MP_USER, AuthSsoInfo.Migration.SSO_MEMBER_TO_ADMIN -> SsoInfo.Migration.TO_MASTER_PASSWORD_USER
            AuthSsoInfo.Migration.MP_USER_TO_SSO_MEMBER -> SsoInfo.Migration.TO_SSO_MEMBER
        }
    }
)