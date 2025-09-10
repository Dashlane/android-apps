package com.dashlane.passwordstrength

import com.nulabinc.zxcvbn.Zxcvbn
import java.lang.IllegalArgumentException
import java.util.Locale

fun passwordStrengthEvaluator() = PasswordStrengthEvaluatorZxcvbn()

class PasswordStrengthEvaluatorZxcvbn : PasswordStrengthEvaluator {
    private val evaluator = Zxcvbn()

    override suspend fun getPasswordStrength(password: String): PasswordStrength {
        return try {
            evaluator.measure(password).let {
                PasswordStrengthZxcvbnResult(
                    score = it.score,
                    feedback = PasswordStrengthZxcvbnResult.Feedback(
                        warning = it.feedback.getWarning(Locale.US),
                        suggestions = it.feedback.getSuggestions(Locale.US),
                    ),
                )
            }.toPasswordStrength()
        } catch (e: IllegalArgumentException) {
            throw PasswordStrengthEvaluatorException("Unable to parse password strength content.", e)
        }
    }

    override suspend fun associatePasswordsWithPasswordStrengthScore(passwords: List<String>): Map<String, PasswordStrengthScore> {
        val distinctPasswords = passwords.distinct()
        val zxcvbnResult = distinctPasswords.map { evaluator.measure(it).score }
        val scores = zxcvbnResult.map(PasswordStrengthScore.Companion::of)
        return distinctPasswords.zip(scores).toMap()
    }
}

private fun PasswordStrengthZxcvbnResult.toPasswordStrength(): PasswordStrength {
    val score = PasswordStrengthScore.of(score)
    val warning = feedback.warning.let { if (it.isEmpty()) null else PasswordStrengthWarning.of(it) }
    val suggestions = feedback.suggestions.map { PasswordStrengthSuggestion.of(it) }
    return PasswordStrength(score, warning, suggestions)
}

data class PasswordStrengthZxcvbnResult(
    val score: Int,
    val feedback: Feedback,
) {
    data class Feedback(
        val warning: String,
        val suggestions: List<String>,
    )
}