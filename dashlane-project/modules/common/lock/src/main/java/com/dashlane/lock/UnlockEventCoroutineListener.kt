package com.dashlane.lock

import kotlinx.coroutines.CompletableDeferred
import java.lang.ref.WeakReference

class UnlockEventCoroutineListener constructor(lockWatcher: LockWatcher) {

    private val unlockDeferredResult = CompletableDeferred<UnlockEvent>()

    init {
        
        Listener(lockWatcher, this)
    }

    suspend fun await(): UnlockEvent {
        return unlockDeferredResult.await()
    }

    private fun onResultLock(unlockEvent: UnlockEvent) {
        unlockDeferredResult.complete(unlockEvent)
    }

    class Listener(
        private val lockWatcher: LockWatcher,
        lockWaiter: UnlockEventCoroutineListener
    ) : LockWatcher.Listener {

        private val lockWatcherRef = WeakReference(lockWaiter)

        init {
            lockWatcher.register(this)
        }

        override fun onLock() {
            
        }

        override fun onUnlockEvent(unlockEvent: UnlockEvent) {
            lockWatcherRef.get()?.onResultLock(unlockEvent)
            unregister()
        }

        private fun unregister() {
            lockWatcher.unregister(this)
        }
    }
}