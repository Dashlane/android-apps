package com.dashlane.lock

import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.shareIn

@Singleton
class LockWatcherImpl @Inject constructor(
    @ApplicationCoroutineScope applicationCoroutineScope: CoroutineScope
) : LockWatcher {

    private val _lockEventSharedFlow = MutableSharedFlow<LockEvent>()
    override val lockEventSharedFlow = _lockEventSharedFlow.shareIn(applicationCoroutineScope, started = SharingStarted.Eagerly, replay = 1)
    override val lockEventFlow: Flow<LockEvent> = _lockEventSharedFlow.asSharedFlow()

    private val lockWaiter = LockWaiter()

    fun setLockHelper(lockHelper: LockHelper) {
        lockWaiter.setLockHelper(lockHelper)
    }

    override suspend fun sendLockEvent() {
        _lockEventSharedFlow.emit(LockEvent.Lock)
    }

    override suspend fun sendUnlockEvent(unlockEvent: LockEvent.Unlock) {
        _lockEventSharedFlow.emit(unlockEvent)
    }

    override suspend fun sendLockCancelled() {
        _lockEventSharedFlow.emit(LockEvent.Cancelled)
    }

    override suspend fun waitUnlock() = lockWaiter.waitUnlock()
}
