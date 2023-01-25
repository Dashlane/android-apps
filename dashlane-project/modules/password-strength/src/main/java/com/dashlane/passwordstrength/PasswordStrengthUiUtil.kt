package com.dashlane.passwordstrength

import android.content.Context
import androidx.annotation.ColorRes

fun PasswordStrength.getShortTitle(context: Context): String =
    score.getShortTitle(context)

fun PasswordStrengthScore.getShortTitle(context: Context): String = when (this) {
    PasswordStrengthScore.VERY_UNGUESSABLE -> context.getString(R.string.password_strength_high)
    PasswordStrengthScore.SAFELY_UNGUESSABLE -> context.getString(R.string.password_strength_safe)
    PasswordStrengthScore.SOMEWHAT_GUESSABLE -> context.getString(R.string.password_strength_medium)
    PasswordStrengthScore.VERY_GUESSABLE -> context.getString(R.string.password_strength_low)
    PasswordStrengthScore.TOO_GUESSABLE -> context.getString(R.string.password_strength_very_low)
}

fun PasswordStrength.getStrengthDescription(context: Context): String =
    score.getStrengthDescription(context)

fun PasswordStrengthScore.getStrengthDescription(context: Context): String = when (this) {
    PasswordStrengthScore.VERY_UNGUESSABLE -> context.getString(R.string.password_strength_description_high)
    PasswordStrengthScore.SAFELY_UNGUESSABLE -> context.getString(R.string.password_strength_description_safe)
    PasswordStrengthScore.SOMEWHAT_GUESSABLE -> context.getString(R.string.password_strength_description_medium)
    PasswordStrengthScore.VERY_GUESSABLE -> context.getString(R.string.password_strength_description_low)
    PasswordStrengthScore.TOO_GUESSABLE -> context.getString(R.string.password_strength_description_very_low)
}

fun PasswordStrength.getYourPasswordIsLabel(context: Context): String =
    score.getYourPasswordIsLabel(context)

fun PasswordStrengthScore.getYourPasswordIsLabel(context: Context): String = when (this) {
    PasswordStrengthScore.VERY_UNGUESSABLE -> context.getString(R.string.your_password_strength_is_high)
    PasswordStrengthScore.SAFELY_UNGUESSABLE -> context.getString(R.string.your_password_strength_is_safe)
    PasswordStrengthScore.SOMEWHAT_GUESSABLE -> context.getString(R.string.your_password_strength_is_medium)
    PasswordStrengthScore.VERY_GUESSABLE -> context.getString(R.string.your_password_strength_is_low)
    PasswordStrengthScore.TOO_GUESSABLE -> context.getString(R.string.your_password_strength_is_very_low)
}

@get:ColorRes
val PasswordStrength.borderColorRes
    get() = score.borderColorRes

@get:ColorRes
val PasswordStrengthScore.borderColorRes
    get() = when (this) {
        PasswordStrengthScore.VERY_UNGUESSABLE,
        PasswordStrengthScore.SAFELY_UNGUESSABLE -> R.color.border_positive_standard_idle
        PasswordStrengthScore.SOMEWHAT_GUESSABLE -> R.color.border_warning_standard_idle
        else -> R.color.border_danger_standard_idle
    }

@get:ColorRes
val PasswordStrength.textColorRes
    get() = score.textColorRes

@get:ColorRes
val PasswordStrengthScore.textColorRes
    get() = when (this) {
        PasswordStrengthScore.VERY_UNGUESSABLE,
        PasswordStrengthScore.SAFELY_UNGUESSABLE -> R.color.text_positive_quiet
        PasswordStrengthScore.SOMEWHAT_GUESSABLE -> R.color.text_warning_quiet
        else -> R.color.text_danger_quiet
    }

@get:ColorRes
val PasswordStrength.containerColorRes
    get() = score.containerColorRes

@get:ColorRes
val PasswordStrengthScore.containerColorRes
    get() = when (this) {
        PasswordStrengthScore.VERY_UNGUESSABLE,
        PasswordStrengthScore.SAFELY_UNGUESSABLE -> R.color.container_expressive_positive_catchy_idle
        PasswordStrengthScore.SOMEWHAT_GUESSABLE -> R.color.container_expressive_warning_catchy_idle
        else -> R.color.container_expressive_danger_catchy_idle
    }

val PasswordStrength.isSafeEnoughForSpecialMode
    get() = score.isSafeEnoughForSpecialMode

val PasswordStrengthScore.isSafeEnoughForSpecialMode
    get() = this == PasswordStrengthScore.VERY_UNGUESSABLE