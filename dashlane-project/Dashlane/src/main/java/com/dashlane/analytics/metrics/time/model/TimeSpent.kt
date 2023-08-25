package com.dashlane.analytics.metrics.time.model

import java.time.Duration
import java.time.Instant

class TimeSpent(private val enterTime: Instant) {

    private var leaveTime: Instant? = null

    fun leave() {
        leaveTime = Instant.now()
    }

    fun computeTimeSpent(): Duration =
        Duration.between(enterTime, leaveTime)
}