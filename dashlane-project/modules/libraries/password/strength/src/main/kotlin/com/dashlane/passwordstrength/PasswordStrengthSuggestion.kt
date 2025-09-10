package com.dashlane.passwordstrength

enum class PasswordStrengthSuggestion(val value: String) {
    USE_WORDS("Use a few words, avoid common phrases."),
    USELESS_SYMBOLS_DIGITS_UPPERCASE("No need for symbols, digits, or uppercase letters."),
    ADD_MORE_WORDS("Add another word or two. Uncommon words are better."),
    USE_LONGER_KEYBOARD_PATTERN("Use a longer keyboard pattern with more turns."),
    AVOID_REPEATS("Avoid repeated words and characters."),
    AVOID_SEQUENCES("Avoid sequences."),
    AVOID_PERSONAL_YEARS("Avoid recent years, avoid years that are associated with you."),
    AVOID_PERSONAL_DATES("Avoid dates and years that are associated with you."),
    USELESS_CAPITALIZATION("Capitalization doesn't help very much."),
    USELESS_UPPERCASE("All-uppercase is almost as easy to guess as all-lowercase."),
    USELESS_REVERSED("Reversed words aren't much harder to guess."),
    USELESS_SUBSTITUTION("Predictable substitutions like '@' instead of 'a' don't help very much."),
    ;

    companion object {
        private val suggestionsByValue =
            enumValues<PasswordStrengthSuggestion>().associateBy(PasswordStrengthSuggestion::value)

        fun of(value: String): PasswordStrengthSuggestion =
            requireNotNull(suggestionsByValue[value]) { "Unexpected password strength suggestion value '$value'." }
    }
}