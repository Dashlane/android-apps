package com.dashlane.authentication

enum class SecurityFeature {
    EMAIL_TOKEN,
    TOTP,
    DUO,
    U2F,
    SSO,
    AUTHENTICATOR
}