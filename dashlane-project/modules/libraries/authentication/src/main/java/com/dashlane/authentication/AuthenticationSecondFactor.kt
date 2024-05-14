package com.dashlane.authentication

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AuthenticationSecondFactor : Parcelable {

    abstract val login: String
    abstract val securityFeatures: Set<SecurityFeature>
    abstract val authenticator: Authenticator?

    val isAuthenticatorEnabled get() = authenticator != null

    @Parcelize
    data class EmailToken(
        override val login: String,
        override val authenticator: Authenticator? = null
    ) : AuthenticationSecondFactor() {
        override val securityFeatures: Set<SecurityFeature>
            get() = buildSet {
                add(SecurityFeature.EMAIL_TOKEN)
                if (isAuthenticatorEnabled) {
                    add(SecurityFeature.AUTHENTICATOR)
                }
            }
    }

    @Parcelize
    data class Totp(
        override val login: String,
        override val securityFeatures: Set<SecurityFeature>,
        override val authenticator: Authenticator? = null,
        val duoPush: DuoPush? = null,
        val u2f: U2f? = null,
    ) : AuthenticationSecondFactor() {

        val isDuoPushEnabled
            get() = duoPush != null
        val isU2fEnabled
            get() = u2f != null
    }

    @Parcelize
    data class U2f(
        val login: String,
        val securityFeatures: Set<SecurityFeature>,
        val challenges:
        List<Challenge>
    ) : Parcelable {

        @Parcelize
        data class Challenge(
            val appId: String,
            val challenge: String,
            val version: String,
            val keyHandle: String
        ) : Parcelable
    }

    @Parcelize
    data class DuoPush(
        val login: String,
        val securityFeatures: Set<SecurityFeature>
    ) : Parcelable

    @Parcelize
    data class Authenticator(
        val login: String,
        val securityFeatures: Set<SecurityFeature>
    ) : Parcelable
}
