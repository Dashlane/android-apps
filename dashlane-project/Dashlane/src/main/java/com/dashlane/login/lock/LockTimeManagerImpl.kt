package com.dashlane.login.lock

import android.os.SystemClock
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class LockTimeManagerImpl @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val userPreferencesManager: UserPreferencesManager
) : LockTimeManager {

    override var lockTimeout: Duration? = LockTimeManager.LOCK_TIMEOUT_DEFAULT_VALUE
        set(value) {
            val duration = value?.seconds?.toInt() ?: -1
            userPreferencesManager.putInt(ConstantsPrefs.TIME_OUT_LOCK, duration)
            field = value
        }

    override var lastUnlock: Instant? = null
    override var lastAction: Instant? = null
    private var autoLockJob: Job? = null

    override fun refreshSavedDuration() {
        lockTimeout = userPreferencesManager.getInt(ConstantsPrefs.TIME_OUT_LOCK)
            .takeIf { it != 0 }
            ?.let { Duration.ofSeconds(it.toLong()) } 
            ?: LockTimeManager.LOCK_TIMEOUT_DEFAULT_VALUE
    }

    override fun shouldAuthoriseForView(): Boolean {
        val lastUnlock = lastUnlock ?: return false
        val lockTimeout = lockTimeout ?: return true 
        return elapsedDuration(lastUnlock).abs() < lockTimeout
    }

    override fun startAutoLockGracePeriod(duration: Duration, lockManager: LockManager) {
        stopAutoLockGracePeriod() 
        autoLockJob = applicationCoroutineScope.launch {
            delay(duration.toMillis())
            lockManager.lockWithoutEvents()
            autoLockJob = null
        }
    }

    override fun isInAutoLockGracePeriod(): Boolean {
        return autoLockJob != null
    }

    override fun setLastActionTimestampToNow() {
        lastAction = Instant.ofEpochMilli(SystemClock.elapsedRealtime())
    }

    override fun stopAutoLockGracePeriod() {
        autoLockJob?.cancel()
        autoLockJob = null
    }

    override fun setUnlockTimestampToNow() {
        lastUnlock = Instant.ofEpochMilli(SystemClock.elapsedRealtime())
    }
}
