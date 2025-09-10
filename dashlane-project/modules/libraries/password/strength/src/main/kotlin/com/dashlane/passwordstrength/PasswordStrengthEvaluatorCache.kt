package com.dashlane.passwordstrength

fun PasswordStrengthEvaluator.cache(
    cache: PasswordStrengthCache,
    maxLength: Int? = MAX_LENGTH_TRUNCATED_DEFAULT,
): PasswordStrengthEvaluator =
    PasswordStrengthEvaluatorCacheImpl(this, cache).let { if (maxLength == null) it else it.truncated(maxLength) }

private class PasswordStrengthEvaluatorCacheImpl(
    private val evaluator: PasswordStrengthEvaluator,
    private val cache: PasswordStrengthCache,
) : PasswordStrengthEvaluator {
    override suspend fun getPasswordStrength(password: String): PasswordStrength {
        val strength = cache.strengths[password]
        if (strength != null) return strength

        return computePasswordStrength(password)
    }

    private suspend fun computePasswordStrength(password: String): PasswordStrength {
        val computedStrength = evaluator.getPasswordStrength(password)
        cache.addStrength(password, computedStrength)
        cache.addScore(password, computedStrength.score)
        return computedStrength
    }

    override suspend fun associatePasswordsWithPasswordStrengthScore(passwords: List<String>): Map<String, PasswordStrengthScore> {
        val scores = cache.scores
        val computedPasswords = passwords - scores.keys 
        if (computedPasswords.isEmpty()) return scores 

        
        return scores + computeScores(computedPasswords)
    }

    private suspend fun computeScores(passwords: List<String>): Map<String, PasswordStrengthScore> {
        val scores = evaluator.associatePasswordsWithPasswordStrengthScore(passwords)
        cache.addScores(scores)
        return scores
    }
}