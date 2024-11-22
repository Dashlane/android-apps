package com.dashlane.login.root

import androidx.navigation.NavBackStackEntry
import com.dashlane.navigation.SchemeUtils.destinationRouteClassSimpleName
import kotlinx.serialization.Serializable

sealed interface LocalLoginDestination {
    @Serializable
    data object Biometric : LocalLoginDestination

    @Serializable
    data class Pin(val login: String) : LocalLoginDestination

    @Serializable
    data class Otp2(val login: String, val restoreSession: Boolean = true) : LocalLoginDestination

    @Serializable
    data class SecretTransfer(val login: String) : LocalLoginDestination

    @Serializable
    data object Password : LocalLoginDestination

    @Serializable
    data object PinRecovery : LocalLoginDestination

    @Serializable
    data object Sso : LocalLoginDestination

    @Serializable
    data object LostKey : LocalLoginDestination

    fun NavBackStackEntry?.toLocalLoginDestination() =
        destinationRouteClassSimpleName()?.let {
            when (it) {
                Biometric::class.simpleName -> Biometric
                Pin::class.simpleName -> Pin
                Otp2::class.simpleName -> Otp2
                Password::class.simpleName -> Password
                PinRecovery::class.simpleName -> PinRecovery
                Sso::class.simpleName -> Sso
                LostKey::class.simpleName -> LostKey
                else -> null
            }
        }
}