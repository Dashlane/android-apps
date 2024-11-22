package com.dashlane.lock

import android.os.SystemClock
import com.dashlane.user.Username
import java.time.Duration
import java.time.Instant

interface LockTimeManager {
    var lockTimeout: Duration

    val lastUnlock: Instant?
    val lastAction: Instant?

    fun updateLockTimeout(duration: Duration, username: Username)
    fun refreshSavedDuration(username: Username)

    fun startAutoLockGracePeriod(duration: Duration, lockWithoutEvents: () -> Unit)
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