package com.dashlane.pin.setup

import android.content.Intent

sealed class PinSetupState {
    abstract val data: PinSetupData

    data class Initial(override val data: PinSetupData) : PinSetupState()
    data class PinUpdated(override val data: PinSetupData, val hasError: Boolean = false) : PinSetupState()
}

sealed class PinSetupNavigationState {
    data object Cancel : PinSetupNavigationState()
    data class GoToNext(val pinCode: String) : PinSetupNavigationState()
    data class GoToSystemLockSetting(val intent: Intent) : PinSetupNavigationState()
}

data class PinSetupData(
    val pinCode: String,
    val chosenPin: String = "",
    val isCancellable: Boolean = false,
    val confirming: Boolean = false,
    val isSystemLockSetup: Boolean = true
)