package com.dashlane.passwordstrength

data class PasswordStrength(
    val score: PasswordStrengthScore,
    val warning: PasswordStrengthWarning?,
    val suggestions: List<PasswordStrengthSuggestion>,
) {
    val percentScore
        get() = score.percentValue
}