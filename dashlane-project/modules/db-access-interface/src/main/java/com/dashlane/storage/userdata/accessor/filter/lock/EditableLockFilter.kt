package com.dashlane.storage.userdata.accessor.filter.lock

interface EditableLockFilter : LockFilter {

    var lockFilter: LockFilter

    fun ignoreUserLock() {
        lockFilter = IgnoreLockFilter
    }
}