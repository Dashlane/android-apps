package com.dashlane.lock

import android.app.Activity



interface LockWatcher {

    interface Listener : UnlockListener {
        fun onLock()
    }

    interface UnlockListener {
        fun onUnlockEvent(unlockEvent: UnlockEvent)
    }

    

    fun sendLockEvent()

    

    fun sendUnlockEvent(unlockEvent: UnlockEvent)

    

    fun register(listener: Listener)

    

    fun unregister(listener: Listener)

    

    suspend fun waitUnlock()

    fun waitUnlock(activity: Activity?, unlockListener: UnlockListener)
}