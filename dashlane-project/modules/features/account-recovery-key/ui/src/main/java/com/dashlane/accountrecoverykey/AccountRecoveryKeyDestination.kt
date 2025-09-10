package com.dashlane.accountrecoverykey

import androidx.navigation.NavBackStackEntry
import com.dashlane.navigation.SchemeUtils.destinationRouteClassSimpleName
import kotlinx.serialization.Serializable

sealed interface AccountRecoveryKeyDestination {
    @Serializable
    data object DetailSettings : AccountRecoveryKeyDestination

    @Serializable
    data object Intro : AccountRecoveryKeyDestination

    @Serializable
    data object Generate : AccountRecoveryKeyDestination

    @Serializable
    data object Confirm : AccountRecoveryKeyDestination

    @Serializable
    data object Success : AccountRecoveryKeyDestination

    fun NavBackStackEntry?.toAccountRecoveryKeyNavigation() =
        destinationRouteClassSimpleName()?.let {
            when (it) {
                DetailSettings::class.simpleName -> DetailSettings
                Intro::class.simpleName -> Intro
                Generate::class.simpleName -> Generate
                Confirm::class.simpleName -> Confirm
                Success::class.simpleName -> Success
                else -> null
            }
        }
}