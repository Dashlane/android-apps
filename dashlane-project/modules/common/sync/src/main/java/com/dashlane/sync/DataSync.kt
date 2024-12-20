package com.dashlane.sync

import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.sync.repositories.SyncProgress
import kotlinx.coroutines.flow.SharedFlow

sealed class DataSyncState {
    sealed class Idle : DataSyncState() {
        data object Init : Idle()
        class Failure(val exception: Throwable) : Idle()
        data object Success : Idle()
    }

    data class Active(val progress: SyncProgress) : DataSyncState()
}

interface DataSync {

    val dataSyncState: SharedFlow<DataSyncState>

    fun sync(origin: Trigger = Trigger.SAVE)

    suspend fun awaitSync(origin: Trigger = Trigger.SAVE): Boolean

    suspend fun initialSync()

    fun maySync(): Boolean

    fun markSyncAllowed()
    fun markSyncNotAllowed()
}