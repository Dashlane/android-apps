package com.dashlane.login.lock

import android.os.SystemClock
import java.time.Duration
import java.time.Instant

interface LockTimeManager {
    var lockTimeout: Duration?

    val lastUnlock: Instant?
    val lastAction: Instant?

    fun refreshSavedDuration()

    fun startAutoLockGracePeriod(duration: Duration, lockManager: LockManager)
    fun shouldAuthoriseForView(): Boolean

    fun setLastActionTimestampToNow()

    fun setUnlockTimestampToNow()

    fun isInAutoLockGracePeriod(): Boolean
    fun stopAutoLockGracePeriod()

    fun elapsedDuration(instant: Instant): Duration =
        Duration.between(instant, Instant.ofEpochMilli(SystemClock.elapsedRealtime()))

    companion object {
        const val LOCK_ON_EXIT_DEFAULT_VALUE = false
        val LOCK_TIMEOUT_DEFAULT_VALUE: Duration = Duration.ofMinutes(15)
        val DEFAULT_AUTO_LOCK_GRACE_PERIOD: Duration = Duration.ofSeconds(30)
    }
}