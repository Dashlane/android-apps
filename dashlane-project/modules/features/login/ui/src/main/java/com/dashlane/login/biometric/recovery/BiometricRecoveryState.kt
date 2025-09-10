package com.dashlane.login.biometric.recovery

import com.dashlane.login.LoginStrategy
import com.dashlane.mvvm.State

data class BiometricRecoveryState(
    val progress: Int = 0,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val showReminderDialog: Boolean = false,
) : State

sealed class BiometricRecoveryNavigationState : State {
    data class Success(val strategy: LoginStrategy.Strategy) : BiometricRecoveryNavigationState()

    data object Cancel : BiometricRecoveryNavigationState()
}