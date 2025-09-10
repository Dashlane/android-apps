package com.dashlane.passwordstrength

interface PasswordStrengthEvaluator {
    suspend fun getPasswordStrength(password: String): PasswordStrength

    suspend fun associatePasswordsWithPasswordStrengthScore(passwords: List<String>): Map<String, PasswordStrengthScore>
}

suspend fun PasswordStrengthEvaluator.getPasswordStrengthScore(password: String): PasswordStrengthScore =
    getPasswordStrength(password).score

suspend fun PasswordStrengthEvaluator.associatePasswordsWithPasswordStrengthScore(vararg passwords: String):
    Map<String, PasswordStrengthScore> = associatePasswordsWithPasswordStrengthScore(passwords.toList())