package com.dashlane.login.lock

import android.app.Activity
import androidx.lifecycle.lifecycleScope
import com.dashlane.lock.LockHelper
import com.dashlane.lock.LockWatcher
import com.dashlane.lock.UnlockEvent
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class LockWatcherImpl @Inject constructor(
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope
) : LockWatcher {

    private val listeners = HashSet<LockWatcher.Listener>()
    private val lockWaiter = LockWaiter()

    fun setLockHelper(lockHelper: LockHelper) {
        lockWaiter.setLockHelper(lockHelper)
    }

    override fun sendLockEvent() {
        for (listener in getListeners()) {
            listener.onLock()
        }
    }

    override fun sendUnlockEvent(unlockEvent: UnlockEvent) {
        for (listener in getListeners()) {
            listener.onUnlockEvent(unlockEvent)
        }
    }

    override fun register(listener: LockWatcher.Listener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override fun unregister(listener: LockWatcher.Listener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    override suspend fun waitUnlock() = lockWaiter.waitUnlock()

    override fun waitUnlock(activity: Activity?, unlockListener: LockWatcher.UnlockListener) {
        val scope = if (activity is DashlaneActivity) activity.lifecycleScope else applicationCoroutineScope
        scope.launch(Dispatchers.Main) {
            waitUnlock()
            unlockListener.onUnlockEvent(UnlockEvent(true, UnlockEvent.Reason.AppAccess()))
        }
    }

    private fun getListeners() = synchronized(listeners) { listeners.toTypedArray() }
}
