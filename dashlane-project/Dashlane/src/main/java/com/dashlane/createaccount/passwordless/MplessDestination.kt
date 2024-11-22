package com.dashlane.createaccount.passwordless

import androidx.navigation.NavBackStackEntry
import com.dashlane.navigation.SchemeUtils.destinationRouteClassSimpleName
import kotlinx.serialization.Serializable

sealed interface MplessDestination {
    @Serializable
    data object Info : MplessDestination

    @Serializable
    data object PinSetup : MplessDestination

    @Serializable
    data object BiometricsSetup : MplessDestination

    @Serializable
    data object TermsAndConditions : MplessDestination

    @Serializable
    data object Confirmation : MplessDestination

    fun NavBackStackEntry?.toMpLessDestination() = destinationRouteClassSimpleName()?.let {
        when (it) {
            Info::class.simpleName -> Info
            PinSetup::class.simpleName -> PinSetup
            BiometricsSetup::class.simpleName -> BiometricsSetup
            TermsAndConditions::class.simpleName -> TermsAndConditions
            Confirmation::class.simpleName -> Confirmation
            else -> null
        }
    }
}