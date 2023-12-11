package com.dashlane.createaccount.passwordless.pincodesetup

sealed class PinSetupState {
    abstract val pinCode: String

    data class Default(override val pinCode: String) : PinSetupState()

    data class Transition(override val pinCode: String) : PinSetupState()

    data class Choose(override val pinCode: String, val hasError: Boolean = false) : PinSetupState()

    data class Confirm(override val pinCode: String, val chosenPin: String) : PinSetupState()

    data class GoToNext(override val pinCode: String) : PinSetupState()
}
