package com.dashlane.item.v3.data

import com.dashlane.passwordstrength.PasswordStrength

data class PasswordHealthData(
    val passwordStrength: PasswordStrength?,
    val isCompromised: Boolean,
    val reusedCount: Int,
    val isPasswordEmpty: Boolean
)