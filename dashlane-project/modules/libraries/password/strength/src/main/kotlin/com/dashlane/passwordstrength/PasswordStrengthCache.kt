package com.dashlane.passwordstrength

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PasswordStrengthCache {
    @Volatile
    var strengths = emptyMap<String, PasswordStrength>()
        private set
    private val strengthsMutex = Mutex()

    @Volatile
    var scores = emptyMap<String, PasswordStrengthScore>()
        private set
    private val scoresMutex = Mutex()

    internal suspend fun addStrength(password: String, strength: PasswordStrength) {
        strengthsMutex.withLock {
            strengths = strengths + (password to strength)
        }
    }

    internal suspend fun addScore(password: String, score: PasswordStrengthScore) {
        scoresMutex.withLock {
            scores = scores + (password to score)
        }
    }

    internal suspend fun addScores(scores: Map<String, PasswordStrengthScore>) {
        scoresMutex.withLock {
            this.scores = this.scores + scores
        }
    }
}