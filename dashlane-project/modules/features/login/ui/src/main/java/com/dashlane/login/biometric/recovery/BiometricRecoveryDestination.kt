package com.dashlane.login.biometric.recovery

import androidx.navigation.NavBackStackEntry
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.Biometric
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.ChangeMp
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.Otp
import com.dashlane.login.biometric.recovery.BiometricRecoveryDestination.Recovery
import com.dashlane.navigation.SchemeUtils.destinationRouteClassSimpleName
import kotlinx.serialization.Serializable

sealed interface BiometricRecoveryDestination {
    @Serializable
    data class Biometric(val login: String) : BiometricRecoveryDestination

    @Serializable
    data class Otp(val login: String, val restoreSession: Boolean = false) : BiometricRecoveryDestination

    @Serializable
    data object ChangeMp : BiometricRecoveryDestination

    @Serializable
    data object Recovery : BiometricRecoveryDestination
}

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