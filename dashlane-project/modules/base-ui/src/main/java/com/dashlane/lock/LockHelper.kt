package com.dashlane.lock

import androidx.annotation.IntDef
import java.time.Duration

interface LockHelper : LockNavigationHelper, LockWatcher {
    companion object {
        const val PROMPT_LOCK_REGULAR = 0
        const val PROMPT_LOCK_FOR_ITEM = 1
        const val PROMPT_LOCK_FOR_SETTINGS = 2
    }

    @IntDef(PROMPT_LOCK_REGULAR, PROMPT_LOCK_FOR_ITEM, PROMPT_LOCK_FOR_SETTINGS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LockPrompt

    val isLocked: Boolean
    val hasEnteredMP: Boolean

    fun isLockedOrLogout(): Boolean

    
    fun startAutoLockGracePeriod()
    fun startAutoLockGracePeriod(duration: Duration)
}