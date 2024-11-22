package com.dashlane.lock

import android.os.Parcelable
import com.dashlane.user.Username
import kotlinx.parcelize.Parcelize

sealed interface LockType : Parcelable {
    @Parcelize
    data object MasterPassword : LockType

    @Parcelize
    data object PinCode : LockType

    @Parcelize
    data object Biometric : LockType
}

interface LockTypeManager {
    fun getLocks(username: Username): List<LockType>

    fun addLock(username: Username, lockType: LockType)

    fun removeLock(username: Username, lockType: LockType)
}