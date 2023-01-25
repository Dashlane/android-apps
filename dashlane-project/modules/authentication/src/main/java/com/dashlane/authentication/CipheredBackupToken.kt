package com.dashlane.authentication

import java.time.Duration
import java.time.Instant



data class CipheredBackupToken(
    val token: String,
    val tokenDate: Instant
) {
    fun isValid(now: Instant) = now.isBefore(tokenDate.plus(TOKEN_EXPIRATION_DAYS))

    companion object {
        private val TOKEN_EXPIRATION_DAYS = Duration.ofDays(15)
    }
}