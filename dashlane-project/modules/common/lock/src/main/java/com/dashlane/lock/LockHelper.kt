package com.dashlane.lock

import java.time.Duration

interface LockHelper : LockNavigationHelper, LockWatcher {
    val isLocked: Boolean
    val hasEnteredMP: Boolean

    suspend fun lock()

    fun isLockedOrLogout(): Boolean

    fun isInAppLoginLocked(): Boolean

    
    fun startAutoLockGracePeriod()
    fun startAutoLockGracePeriod(duration: Duration)
}