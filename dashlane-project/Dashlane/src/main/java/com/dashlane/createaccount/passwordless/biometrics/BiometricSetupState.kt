package com.dashlane.createaccount.passwordless.biometrics

sealed class BiometricSetupState {
    object Loading : BiometricSetupState()
    object HardwareEnabled : BiometricSetupState()
    object HardwareDisabled : BiometricSetupState()
}
