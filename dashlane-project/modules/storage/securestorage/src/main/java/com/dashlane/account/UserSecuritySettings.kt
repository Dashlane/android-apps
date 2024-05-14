package com.dashlane.account

import androidx.annotation.IntDef

data class UserSecuritySettings(
    val isToken: Boolean = false,
    val isTotp: Boolean = false,
    val isOtp2: Boolean = false,
    val isDuoEnabled: Boolean = false,
    val isU2fEnabled: Boolean = false,
    val isSso: Boolean = false,
    val isAuthenticatorEnabled: Boolean = false
) {
    constructor(@UserSecurityFlags flags: Int) : this(
        flags == SECURITY_TOKEN,
        (flags and SECURITY_TOTP) != 0,
        (flags and SECURITY_TOTP_2) != 0,
        (flags and SECURITY_FLAG_DUO) != 0,
        (flags and SECURITY_FLAG_U2F) != 0,
        (flags and SECURITY_FLAG_SSO) != 0,
        (flags and SECURITY_FLAG_AUTHENTICATOR) != 0
        )

    
    fun asFlags(): Int {
        val features = arrayOf(
            isToken to SECURITY_TOKEN,
            isTotp to SECURITY_TOTP,
            isOtp2 to SECURITY_TOTP_2,
            isDuoEnabled to SECURITY_FLAG_DUO,
            isU2fEnabled to SECURITY_FLAG_U2F,
            isSso to SECURITY_FLAG_SSO,
            isAuthenticatorEnabled to SECURITY_FLAG_AUTHENTICATOR
        )
        return features
            .filter(Pair<Boolean, Int>::first)
            .map(Pair<Boolean, Int>::second)
            .reduce(Int::or)
    }

    fun asString(otp2: Boolean = false): String {
        val type = when {
            isToken -> "token"
            isTotp || isU2fEnabled || isDuoEnabled -> if (otp2) "OTP2" else "OTP1"
            isOtp2 -> "OTP2"
            isSso -> "SSO"
            else -> ""
        }
        return if (isDuoEnabled) {
            type + "AndDuo"
        } else {
            type
        }
    }

    @IntDef(
        value = [
            SECURITY_TOKEN,
            SECURITY_TOTP,
            SECURITY_TOTP_2,
            SECURITY_FLAG_DUO,
            SECURITY_FLAG_U2F,
            SECURITY_FLAG_SSO,
            SECURITY_FLAG_AUTHENTICATOR
        ],
        flag = true
    )
    annotation class UserSecurityFlags

    companion object {
        const val SECURITY_TOKEN = 1
        const val SECURITY_TOTP = 1 shl 1
        const val SECURITY_TOTP_2 = 1 shl 2
        const val SECURITY_FLAG_DUO = 1 shl 3

        
        const val SECURITY_FLAG_U2F = 1 shl 5
        const val SECURITY_FLAG_SSO = 1 shl 6
        const val SECURITY_FLAG_AUTHENTICATOR = 1 shl 7
    }
}