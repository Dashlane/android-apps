package com.dashlane.storage.userdata.accessor.filter.lock

object IgnoreLockFilter : LockFilter {
    override val requireUserUnlock: Boolean = false
}