package com.dashlane.masterpassword.compose

import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.design.component.PasswordStrengthIndicator
import com.dashlane.passwordstrength.PasswordStrengthScore

sealed class ChangeMasterPasswordState {
    abstract val data: ChangeMasterPasswordData

    data class Initial(override val data: ChangeMasterPasswordData) : ChangeMasterPasswordState()
    data class NavigateBack(override val data: ChangeMasterPasswordData) : ChangeMasterPasswordState()
    data class PasswordChanged(override val data: ChangeMasterPasswordData) : ChangeMasterPasswordState()
    data class ConfimPassword(override val data: ChangeMasterPasswordData) : ChangeMasterPasswordState()
    data class ConfimPasswordChanged(override val data: ChangeMasterPasswordData) : ChangeMasterPasswordState()
    data class Finish(override val data: ChangeMasterPasswordData, val newMasterPassword: ObfuscatedByteArray) : ChangeMasterPasswordState()
    data class NotStrongEnough(override val data: ChangeMasterPasswordData) : ChangeMasterPasswordState()
}

data class ChangeMasterPasswordData(
    val password: String = "",
    val passwordStrengthScore: PasswordStrengthScore? = null,
    val passwordStrength: PasswordStrengthIndicator.Strength? = null,
    val confirmPassword: String = "",
    val isNextEnabled: Boolean = false,
    val isConfirming: Boolean = false,
    val isMatching: Boolean = false
)
