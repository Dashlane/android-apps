package com.dashlane.createaccount.passwordless.pincodesetup

import android.content.Intent

sealed class PinSetupState {
    abstract val data: PinSetupData

    data class Initial(override val data: PinSetupData) : PinSetupState()
    data class PinUpdated(override val data: PinSetupData, val hasError: Boolean = false) : PinSetupState()
    data class GoToNext(override val data: PinSetupData) : PinSetupState()
    data class GoToSystemLockSetting(override val data: PinSetupData, val intent: Intent) : PinSetupState()
}

data class PinSetupData(
    val pinCode: String,
    val chosenPin: String = "",
    val confirming: Boolean = false,
    val isSystemLockSetup: Boolean = true
)