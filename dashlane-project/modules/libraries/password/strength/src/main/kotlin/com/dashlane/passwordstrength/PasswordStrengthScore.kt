package com.dashlane.passwordstrength

enum class PasswordStrengthScore(val value: Int) {
    TOO_GUESSABLE(0),

    VERY_GUESSABLE(1),

    SOMEWHAT_GUESSABLE(2),

    SAFELY_UNGUESSABLE(3),

    VERY_UNGUESSABLE(4),
    ;

    val percentValue: Int
        get() = value * 25

    companion object {
        private val scoresArray = arrayOf(
            TOO_GUESSABLE,
            VERY_GUESSABLE,
            SOMEWHAT_GUESSABLE,
            SAFELY_UNGUESSABLE,
            VERY_UNGUESSABLE,
        )

        fun of(value: Int): PasswordStrengthScore {
            require(value in scoresArray.indices) { "Unexpected password strength score value $value." }
            return scoresArray[value]
        }
    }
}