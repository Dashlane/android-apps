package com.dashlane.data.login

data class SsoInfo(
    val login: String,
    val serviceProviderUrl: String,
    val isNitroProvider: Boolean,
    val migration: Migration?,
) {
    enum class Migration { TO_SSO_MEMBER, TO_MASTER_PASSWORD_USER }
}