package com.dashlane.changemasterpassword

import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.design.component.PasswordStrengthIndicator
import com.dashlane.mvvm.State
import com.dashlane.passwordstrength.PasswordStrengthScore

sealed class ChangeMasterPasswordState : State {
    data class View(
        val password: String = "",
        val passwordStrengthScore: PasswordStrengthScore? = null,
        val passwordStrength: PasswordStrengthIndicator.Strength? = null,
        val confirmPassword: String = "",
        val isNextEnabled: Boolean = false,
        val isConfirming: Boolean = false,
        val isMatching: Boolean = false,
        val isTipsShown: Boolean = false
    ) : ChangeMasterPasswordState(), State.View

    sealed class SideEffect : ChangeMasterPasswordState(), State.SideEffect {
        data object NavigateBack : SideEffect()
        data class Finish(val newMasterPassword: ObfuscatedByteArray) : SideEffect()
    }
}
