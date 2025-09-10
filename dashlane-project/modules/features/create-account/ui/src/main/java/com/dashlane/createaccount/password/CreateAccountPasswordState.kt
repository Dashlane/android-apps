package com.dashlane.createaccount.password

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.design.component.UiPasswordStrength
import com.dashlane.mvvm.State
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.udf.Event

data class CreateAccountPasswordData(
    val email: String,
    val isB2B: Boolean,
    val password: TextFieldValue = TextFieldValue(""),
    val confirmPassword: TextFieldValue = TextFieldValue(""),
    val passwordStrengthScore: PasswordStrengthScore? = null,
    val passwordStrength: UiPasswordStrength? = null,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: CreateAccountPasswordError? = null,
    val isMatching: Boolean = false,
    val isTipsShown: Boolean = false,
)

sealed class CreateAccountPasswordState : State {
    sealed class View : CreateAccountPasswordState(), State.View {
        abstract val data: CreateAccountPasswordData

        data class Initial(override val data: CreateAccountPasswordData) : View()

        data class CheckPasswordStrength(override val data: CreateAccountPasswordData) : View()

        data class DisplayPassword(override val data: CreateAccountPasswordData) : View()

        data class DisplayTips(override val data: CreateAccountPasswordData) : View()

        data class TransitionToConfirm(override val data: CreateAccountPasswordData) : View()

        data class DisplayPasswordConfirm(override val data: CreateAccountPasswordData) : View()

        data class SavePassword(override val data: CreateAccountPasswordData) : View()
    }

    sealed class SideEffect : CreateAccountPasswordState(), State.SideEffect {
        data object GoToBiometricSetup : SideEffect()

        data object GoToTermsAndConditions : SideEffect()

        data class GoToPasswordless(val email: String) : SideEffect()

        data object Cancel : SideEffect()
    }
}

sealed class CreateAccountPasswordEvent : Event {
    data class PasswordChanged(val password: TextFieldValue) : CreateAccountPasswordEvent()

    data class ConfirmPasswordChanged(val confirmPassword: TextFieldValue) : CreateAccountPasswordEvent()

    data class PasswordStrengthChanged(
        val passwordStrengthScore: PasswordStrengthScore,
        val passwordStrength: UiPasswordStrength,
    ) : CreateAccountPasswordEvent()

    data class PasswordSaved(val hasBiometrics: Boolean) : CreateAccountPasswordEvent()

    data object NextClicked : CreateAccountPasswordEvent()

    data object TipsClicked : CreateAccountPasswordEvent()

    data object PasswordlessClicked : CreateAccountPasswordEvent()

    data object BottomSheetDismissed : CreateAccountPasswordEvent()

    data object BackPressed : CreateAccountPasswordEvent()

    data object FocusChanged : CreateAccountPasswordEvent()

    data class Error(val error: CreateAccountPasswordError) : CreateAccountPasswordEvent()
}

sealed class CreateAccountPasswordError {
    data object Generic : CreateAccountPasswordError()

    data object Network : CreateAccountPasswordError()
}