package com.dashlane.changemasterpassword

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.design.component.UiPasswordStrength
import com.dashlane.mvvm.State
import com.dashlane.passwordstrength.PasswordStrengthScore

sealed class ChangeMasterPasswordState : State {
    data class View(
        val password: TextFieldValue = TextFieldValue(""),
        val passwordStrengthScore: PasswordStrengthScore? = null,
        val passwordStrength: UiPasswordStrength? = null,
        val confirmPassword: TextFieldValue = TextFieldValue(""),
        val isNextEnabled: Boolean = false,
        val isConfirming: Boolean = false,
        val isMatching: Boolean = false,
        val isTipsShown: Boolean = false,
    ) : ChangeMasterPasswordState(), State.View

    sealed class SideEffect : ChangeMasterPasswordState(), State.SideEffect {
        data object NavigateBack : SideEffect()

        data object Finish : SideEffect()
    }
}