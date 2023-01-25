package com.dashlane.storage.userdata.accessor.filter.lock



object DefaultLockFilter : LockFilter {
    override val requireUserUnlock: Boolean = true
}