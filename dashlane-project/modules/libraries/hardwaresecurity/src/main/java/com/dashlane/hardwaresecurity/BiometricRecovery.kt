package com.dashlane.hardwaresecurity

import kotlinx.coroutines.flow.SharedFlow

interface BiometricRecovery {
    val biometricRecoveryStatusFlow: SharedFlow<Boolean>

    var isFeatureKnown: Boolean

    fun isFeatureAvailable(): Boolean

    fun isFeatureEnabled(): Boolean

    fun setBiometricRecoveryFeatureEnabled(enabled: Boolean)

    suspend fun setBiometricRecoveryFeatureEnabledAsync(enabled: Boolean)

    fun isSetUpForUser(username: String): Boolean
}