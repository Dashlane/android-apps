package com.dashlane.createaccount.biometric

import com.dashlane.mvvm.State
import com.dashlane.udf.Event

data class CreateAccountBiometricData(
    val biometricChecked: Boolean = true,
    val biometricRecoveryChecked: Boolean = true,
    val helpShown: Boolean = false,
    val snackBarShown: Boolean = false,
)

sealed class CreateAccountBiometricState : State {
    sealed class View : CreateAccountBiometricState(), State.View {
        abstract val data: CreateAccountBiometricData

        data class Initial(override val data: CreateAccountBiometricData) : View()

        data class Loading(override val data: CreateAccountBiometricData) : View()
    }

    sealed class SideEffect : CreateAccountBiometricState(), State.SideEffect {
        data object Success : SideEffect()
    }
}

sealed class CreateAccountBiometricEvent : Event {
    data class BiometricCheckChanged(val isChecked: Boolean) : CreateAccountBiometricEvent()

    data class BiometricRecoveryCheckChanged(val isChecked: Boolean) : CreateAccountBiometricEvent()

    data object NextClicked : CreateAccountBiometricEvent()

    data object HelpClicked : CreateAccountBiometricEvent()

    data object Success : CreateAccountBiometricEvent()

    data class Error(val error: CreateAccountBiometricError) : CreateAccountBiometricEvent()
}

sealed class CreateAccountBiometricError {
    data object Generic : CreateAccountBiometricError()
}