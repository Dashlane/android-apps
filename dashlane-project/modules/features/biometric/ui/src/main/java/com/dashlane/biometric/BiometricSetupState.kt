package com.dashlane.biometric

import androidx.biometric.BiometricPrompt
import com.dashlane.mvvm.State
import com.dashlane.udf.Event

data class BiometricSetupData(
    val isBiometricRecovery: Boolean = false,
    val authenticator: Int = -1,
    val showBiometricPrompt: Boolean = false,
    val cryptoObject: BiometricPrompt.CryptoObject? = null,
    val error: BiometricSetupError? = null,
    val showBiometricSetupBottomSheet: Boolean = false,
    val showBiometricRecoverySetupBottomSheet: Boolean = false,
) : State.View

sealed class BiometricSetupState : State {
    sealed class View : BiometricSetupState(), State.View {
        abstract val data: BiometricSetupData

        data class Initial(override val data: BiometricSetupData) : View()

        data class Loading(override val data: BiometricSetupData) : View()

        data class LoadingRecovery(override val data: BiometricSetupData) : View()

        data class DisplayBiometricPrompt(override val data: BiometricSetupData) : View()

        data class DisplayBottomSheet(override val data: BiometricSetupData) : View()

        data class SaveBiometric(override val data: BiometricSetupData) : View()

        data class SaveBiometricRecovery(override val data: BiometricSetupData) : View()

        data class Error(override val data: BiometricSetupData) : View()
    }

    sealed class SideEffect : BiometricSetupState(), State.SideEffect {
        data object Success : SideEffect()

        data object GoToSettings : SideEffect()

        data object Cancel : SideEffect()

        data class CancelWithToast(val code: Int?, val message: String?) : SideEffect()
    }
}

sealed class BiometricSetupEvent : Event {
    data object ViewResumed : BiometricSetupEvent()

    data class Success(val goToRecovery: Boolean) : BiometricSetupEvent()

    data class AuthenticationSuccess(val cryptoObject: BiometricPrompt.CryptoObject?) : BiometricSetupEvent()

    data object AuthenticationFailed : BiometricSetupEvent()

    data class AuthenticationError(val code: Int, val message: String) : BiometricSetupEvent()

    data class BiometricChecked(val authenticator: Int, val cryptoObject: BiometricPrompt.CryptoObject?) : BiometricSetupEvent()

    data object BiometricRecoveryChecked : BiometricSetupEvent()

    data class DialogPositive(val error: BiometricSetupError) : BiometricSetupEvent()

    data object RecoverySetupPositive : BiometricSetupEvent()

    data object BiometricSetupPositive : BiometricSetupEvent()

    data object DialogDismissed : BiometricSetupEvent()

    data class Error(val error: BiometricSetupError) : BiometricSetupEvent()

    data object Cancel : BiometricSetupEvent()
}

sealed class BiometricSetupError {
    data object BiometricNotRegistered : BiometricSetupError()

    data object RequireStrongerBiometric : BiometricSetupError()
}