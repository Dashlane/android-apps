package com.dashlane.util.time

class TimeTicker {
    private var lastTick = System.currentTimeMillis()

    fun tick(): Long {
        val now = System.currentTimeMillis()
        val durationSinceLastTick = now - lastTick
        lastTick = now
        return durationSinceLastTick
    }
}