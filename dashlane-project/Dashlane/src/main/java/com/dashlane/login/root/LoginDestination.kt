package com.dashlane.login.root

import kotlinx.serialization.Serializable

sealed interface LoginDestination {
    @Serializable
    data class Email(val login: String? = null, val allowSkipEmail: Boolean) : LoginDestination

    @Serializable
    data class Otp(val login: String, val restoreSession: Boolean = false) : LoginDestination

    @Serializable
    data class SecretTransfer(
        val login: String? = null,
        val showQrCode: Boolean
    ) : LoginDestination

    @Serializable
    data object Password : LoginDestination

    @Serializable
    data class Token(val login: String) : LoginDestination
}