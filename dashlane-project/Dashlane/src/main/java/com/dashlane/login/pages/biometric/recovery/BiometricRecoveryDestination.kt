package com.dashlane.login.pages.biometric.recovery

import androidx.navigation.NavBackStackEntry
import com.dashlane.navigation.SchemeUtils.destinationRouteClassSimpleName
import kotlinx.serialization.Serializable

sealed interface BiometricRecoveryDestination {
    @Serializable
    data object Biometric : BiometricRecoveryDestination

    @Serializable
    data class Otp(val login: String, val restoreSession: Boolean = false) : BiometricRecoveryDestination

    @Serializable
    data object ChangeMp : BiometricRecoveryDestination

    @Serializable
    data object Recovery : BiometricRecoveryDestination

    fun NavBackStackEntry?.toBiometricRecoveryDestination() =
        destinationRouteClassSimpleName()?.let {
            when (it) {
                Biometric::class.simpleName -> Biometric
                Otp::class.simpleName -> Otp
                ChangeMp::class.simpleName -> ChangeMp
                Recovery::class.simpleName -> Recovery
                else -> null
            }
        }
}