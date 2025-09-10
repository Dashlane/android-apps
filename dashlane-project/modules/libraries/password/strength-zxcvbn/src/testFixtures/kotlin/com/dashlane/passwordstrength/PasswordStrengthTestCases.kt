package com.dashlane.passwordstrength

import java.util.stream.Stream

object PasswordStrengthTestCases {
    fun passwordsToStrengths(): Stream<Pair<String, PasswordStrength>> =
        Stream.concat(passwordsToStrengthsCustom(), passwordsToStrengthsLibraryTests())

    private fun passwordsToStrengthsCustom() = Stream.of(
        "" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = null,
            suggestions = listOf(
                PasswordStrengthSuggestion.USE_WORDS,
                PasswordStrengthSuggestion.USELESS_SYMBOLS_DIGITS_UPPERCASE,
            ),
        ),
        " " to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "!" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "abc" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.SEQUENCES,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_SEQUENCES,
            ),
        ),
        "admin" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "Azerty12" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_CAPITALIZATION,
            ),
        ),
        "Dashlane12" to PasswordStrength(
            score = PasswordStrengthScore.SAFELY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "FtDjR!310!UH" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "Engaging42Ferment87Biology" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.REPEATS_CHARACTER,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_REPEATS,
            ),
        ),
        "tcP0DcmjjiixD3Umh2V6-SeNNNuDASZs0lGfU2w4GZiUM58JVt" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "tcP0DcmjjiixD3Umh2V6-SeNNNuDASZs0lGfU2w4GZiUM58JVtqZW-zYtqnJWxWi1QW9xUl4_cA19abm" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "2026" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.RECENT_YEARS,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_PERSONAL_YEARS,
            ),
        ),
        "abcabc" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.REPEATS_SEQUENCE,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_REPEATS,
            ),
        ),
        "junit5junit51" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = PasswordStrengthWarning.REPEATS_SEQUENCE,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_REPEATS,
            ),
        ),
        "tribunal" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.WORD,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
            ),
        ),
        "lanubirt" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.WORD,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_REVERSED,
            ),
        ),
    )

    private fun passwordsToStrengthsLibraryTests() = Stream.of(
        "zxcvbn" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.TOP_100_COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "qwER43@!" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = PasswordStrengthWarning.SHORT_KEYBOARD_PATTERNS,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USE_LONGER_KEYBOARD_PATTERN,
            ),
        ),
        "Tr0ub4dour&3" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = null,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_CAPITALIZATION,
                PasswordStrengthSuggestion.USELESS_SUBSTITUTION,
            ),
        ),
        "correcthorsebatterystaple" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "coRrecth0rseba++ery9.23.2007staple$" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "p@ssword" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.SIMILAR_TO_COMMON,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_SUBSTITUTION,
            ),
        ),
        "p@\$\$word" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.SIMILAR_TO_COMMON,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_SUBSTITUTION,
            ),
        ),
        "123456" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.TOP_10_COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "123456789" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.TOP_10_COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "11111111" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.TOP_100_COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "zxcvbnm,./" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.ROWS_OF_KEYS,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USE_LONGER_KEYBOARD_PATTERN,
            ),
        ),
        "love88" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.SIMILAR_TO_COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "angel08" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON_NAMES,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_SUBSTITUTION,
            ),
        ),
        "monkey13" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "iloveyou" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.TOP_100_COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "woaini" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "wang" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.NAMES,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "tianya" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON_NAMES,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "zhang198822" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = PasswordStrengthWarning.DATES,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_PERSONAL_DATES,
            ),
        ),
        "li4478" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "a6a4Aa8a" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "b6b4Bb8b" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "z6z4Zz8z" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "aiIiAaIA" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "zxXxZzXZ" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "pässwörd" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = null,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "alpha bravo charlie delta" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "a b c d e f g h i j k l m n o p q r s t u v w x y z 0 1 2 3 4 5 6 7 8 9" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "a b c 1 2 3" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "correct-horse-battery-staple" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "correct.horse.battery.staple" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "correct,horse,battery,staple" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "correct~horse~battery~staple" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "WhyfaultthebardifhesingstheArgives’harshfate?" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "Eupithes’sonAntinousbroketheirsilence" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "Athena lavished a marvelous splendor" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "buckmulliganstenderchant" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "seethenthatyewalkcircumspectly" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "LihiandthepeopleofMorianton" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "establishedinthecityofZarahemla" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "!\"£$%^&*()" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = PasswordStrengthWarning.ROWS_OF_KEYS,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USE_LONGER_KEYBOARD_PATTERN,
            ),
        ),
        "D0g.................." to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.REPEATS_CHARACTER,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_REPEATS,
            ),
        ),
        "abcdefghijk987654321" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.SEQUENCES,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_SEQUENCES,
            ),
        ),
        "neverforget13/3/1997" to PasswordStrength(
            score = PasswordStrengthScore.SAFELY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "1qaz2wsx3edc" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "temppass22" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.SIMILAR_TO_COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "briansmith" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON_NAMES,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "briansmith4mayor" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "password1" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "viking" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "thx1138" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "ScoRpi0ns" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = null,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_SUBSTITUTION,
            ),
        ),
        "do you know" to PasswordStrength(
            score = PasswordStrengthScore.SAFELY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "ryanhunter2000" to PasswordStrength(
            score = PasswordStrengthScore.SAFELY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "rianhunter2000" to PasswordStrength(
            score = PasswordStrengthScore.SAFELY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "asdfghju7654rewq" to PasswordStrength(
            score = PasswordStrengthScore.SAFELY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "AOEUIDHG&*()LS_" to PasswordStrength(
            score = PasswordStrengthScore.SAFELY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "12345678" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.TOP_10_COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "defghi6789" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.SEQUENCES,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.AVOID_SEQUENCES,
            ),
        ),
        "rosebud" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "Rosebud" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_CAPITALIZATION,
            ),
        ),
        "ROSEBUD" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_UPPERCASE,
            ),
        ),
        "rosebuD" to PasswordStrength(
            score = PasswordStrengthScore.TOO_GUESSABLE,
            warning = PasswordStrengthWarning.COMMON,
            suggestions = listOf(PasswordStrengthSuggestion.ADD_MORE_WORDS),
        ),
        "ros3bud99" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.SIMILAR_TO_COMMON,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_SUBSTITUTION,
            ),
        ),
        "r0s3bud99" to PasswordStrength(
            score = PasswordStrengthScore.VERY_GUESSABLE,
            warning = PasswordStrengthWarning.SIMILAR_TO_COMMON,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_SUBSTITUTION,
            ),
        ),
        "R0\$38uD99" to PasswordStrength(
            score = PasswordStrengthScore.SOMEWHAT_GUESSABLE,
            warning = null,
            suggestions = listOf(
                PasswordStrengthSuggestion.ADD_MORE_WORDS,
                PasswordStrengthSuggestion.USELESS_SUBSTITUTION,
            ),
        ),
        "verlineVANDERMARK" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "eheuczkqyq" to PasswordStrength(
            score = PasswordStrengthScore.SAFELY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "rWibMFACxAUGZmxhVncy" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
        "Ba9ZyWABu99[BK#6MBgbH88Tofv)vs\$w" to PasswordStrength(
            score = PasswordStrengthScore.VERY_UNGUESSABLE,
            warning = null,
            suggestions = emptyList(),
        ),
    )
}