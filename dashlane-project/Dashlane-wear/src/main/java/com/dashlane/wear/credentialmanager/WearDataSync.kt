package com.dashlane.wear.credentialmanager

import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.sync.DataSync
import com.dashlane.sync.DataSyncState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class WearDataSync @Inject constructor() : DataSync {
    override val dataSyncState: SharedFlow<DataSyncState>
        get() = MutableSharedFlow()

    override fun sync(origin: Trigger) {
        throw UnsupportedOperationException()
    }

    override suspend fun awaitSync(origin: Trigger): Boolean {
        throw UnsupportedOperationException()
    }

    override suspend fun initialSync() {
        throw UnsupportedOperationException()
    }

    override fun maySync(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun markSyncAllowed() {
        throw UnsupportedOperationException()
    }

    override fun markSyncNotAllowed() {
        throw UnsupportedOperationException()
    }
}