package com.dashlane.login.lock

import androidx.annotation.IntDef
import com.dashlane.lock.LockHelper
import com.dashlane.lock.UnlockEvent

interface LockTypeManager {
    companion object {
        const val LOCK_TYPE_UNSPECIFIED = -1
        const val LOCK_TYPE_MASTER_PASSWORD = 0
        const val LOCK_TYPE_PIN_CODE = 1
        const val LOCK_TYPE_BIOMETRIC = 2
    }

    @IntDef(
        LOCK_TYPE_MASTER_PASSWORD,
        LOCK_TYPE_PIN_CODE,
        LOCK_TYPE_BIOMETRIC
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class LockType

    @LockType
    fun getLockType(): Int

    @LockType
    fun getLockType(onlyIfAllow: Boolean): Int

    @LockType
    fun getLockType(@LockHelper.LockPrompt lockPrompt: Int): Int

    fun setLockType(@LockType lockType: Int)

    fun isItemUnlockableByPinOrFingerprint(): Boolean

    fun shouldEnterMasterPassword(unlockReason: UnlockEvent.Reason?): Boolean

    fun resetLockoutTime()
}