package com.dashlane.passwordstrength

enum class PasswordStrengthWarning(val value: String) {
    ROWS_OF_KEYS("Straight rows of keys are easy to guess."),
    SHORT_KEYBOARD_PATTERNS("Short keyboard patterns are easy to guess."),
    REPEATS_SEQUENCE("Repeats like \"abcabcabc\" are only slightly harder to guess than \"abc\"."),
    REPEATS_CHARACTER("Repeats like \"aaa\" are easy to guess."),
    SEQUENCES("Sequences like abc or 6543 are easy to guess."),
    RECENT_YEARS("Recent years are easy to guess."),
    DATES("Dates are often easy to guess."),
    TOP_10_COMMON("This is a top-10 common password."),
    TOP_100_COMMON("This is a top-100 common password."),
    COMMON("This is a very common password."),
    SIMILAR_TO_COMMON("This is similar to a commonly used password."),
    WORD("A word by itself is easy to guess."),
    NAMES("Names and surnames by themselves are easy to guess."),
    COMMON_NAMES("Common names and surnames are easy to guess."),
    ;

    companion object {
        private val warningsByValue =
            enumValues<PasswordStrengthWarning>().associateBy(PasswordStrengthWarning::value)

        fun of(value: String): PasswordStrengthWarning =
            requireNotNull(warningsByValue[value]) { "Unexpected password strength warning value '$value'." }
    }
}