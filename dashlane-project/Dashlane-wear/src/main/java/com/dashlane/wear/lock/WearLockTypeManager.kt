package com.dashlane.wear.lock

import com.dashlane.lock.LockType
import com.dashlane.lock.LockTypeManager
import com.dashlane.user.Username
import javax.inject.Inject

class WearLockTypeManager @Inject constructor() : LockTypeManager {
    
    override fun getLocks(username: Username): List<LockType> = emptyList()

    override fun addLock(
        username: Username,
        lockType: LockType,
    ) {
        throw UnsupportedOperationException()
    }

    override fun removeLock(
        username: Username,
        lockType: LockType,
    ) {
        throw UnsupportedOperationException()
    }
}