package com.dashlane.autofill.pause.model

import java.time.Duration
import java.time.Instant

enum class PauseDurations(private val duration: Duration) {
    ONE_HOUR(Duration.ofHours(1L)),
    ONE_DAY(Duration.ofDays(1L)),
    PERMANENT(Duration.ZERO);

    fun getInstantForDuration(): Instant {
        if (duration == Duration.ZERO) {
            return Instant.MAX
        }

        return Instant.now().plus(duration)
    }
}
