package com.dashlane.login.lock

import com.dashlane.lock.LockHelper
import kotlinx.coroutines.delay

class LockWaiter {

    private var lockHelper: LockHelper? = null

    fun setLockHelper(lockHelper: LockHelper) {
        this.lockHelper = lockHelper
    }

    suspend fun waitUnlock() {
        while (lockHelper?.isLocked == true) {
            
            delay(10)
        }
    }
}