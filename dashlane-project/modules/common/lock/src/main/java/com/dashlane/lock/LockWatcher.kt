package com.dashlane.lock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface LockWatcher {

    val lockEventSharedFlow: SharedFlow<LockEvent>

    val lockEventFlow: Flow<LockEvent>

    suspend fun sendLockEvent()

    suspend fun sendUnlockEvent(unlockEvent: LockEvent.Unlock)

    suspend fun sendLockCancelled()

    suspend fun waitUnlock()
}