package com.dashlane.passwordstrength

internal const val MAX_LENGTH_TRUNCATED_DEFAULT = 50

fun PasswordStrengthEvaluator.truncated(maxLength: Int = MAX_LENGTH_TRUNCATED_DEFAULT): PasswordStrengthEvaluator =
    PasswordStrengthEvaluatorTruncatedImpl(this, maxLength)

private class PasswordStrengthEvaluatorTruncatedImpl(
    private val evaluator: PasswordStrengthEvaluator,
    private val maxLength: Int,
) : PasswordStrengthEvaluator {
    init {
        require(maxLength >= 1) { "Requested max length $maxLength is less than zero." }
    }

    override suspend fun getPasswordStrength(password: String): PasswordStrength {
        val truncatedPassword = password.take(maxLength)
        return evaluator.getPasswordStrength(truncatedPassword)
    }

    override suspend fun associatePasswordsWithPasswordStrengthScore(passwords: List<String>): Map<String, PasswordStrengthScore> {
        val truncatedPasswords = passwords.map { it.take(maxLength) }
        val scoresByTruncatedPasswords =
            evaluator.associatePasswordsWithPasswordStrengthScore(truncatedPasswords)
        return passwords.mapIndexed { index, s -> s to scoresByTruncatedPasswords.getValue(truncatedPasswords[index]) }.toMap()
    }
}