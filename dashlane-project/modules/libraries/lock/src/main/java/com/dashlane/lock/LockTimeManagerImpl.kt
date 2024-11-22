package com.dashlane.lock

import android.os.SystemClock
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.user.Username
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockTimeManagerImpl @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val preferencesManager: PreferencesManager
) : LockTimeManager {

    override var lockTimeout: Duration = LockTimeManager.LOCK_TIMEOUT_DEFAULT_VALUE

    override var lastUnlock: Instant? = null
    override var lastAction: Instant? = null
    private var autoLockJob: Job? = null

    override fun updateLockTimeout(duration: Duration, username: Username) {
        this.lockTimeout = duration
        preferencesManager[username].putInt(ConstantsPrefs.TIME_OUT_LOCK, duration.seconds.toInt())
    }

    override fun refreshSavedDuration(username: Username) {
        lockTimeout = preferencesManager[username].getInt(ConstantsPrefs.TIME_OUT_LOCK)
            .takeIf { it != 0 }
            ?.let { Duration.ofSeconds(it.toLong()) } 
            ?: LockTimeManager.LOCK_TIMEOUT_DEFAULT_VALUE
    }

    override fun shouldAuthoriseForView(): Boolean {
        val lastUnlock = lastUnlock ?: return false
        return elapsedDuration(lastUnlock).abs() < lockTimeout
    }

    override fun startAutoLockGracePeriod(duration: Duration, lockWithoutEvents: () -> Unit) {
        stopAutoLockGracePeriod() 
        autoLockJob = applicationCoroutineScope.launch {
            delay(duration.toMillis())
            lockWithoutEvents()
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
