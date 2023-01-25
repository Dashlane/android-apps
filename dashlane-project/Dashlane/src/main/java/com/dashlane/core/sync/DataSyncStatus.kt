package com.dashlane.core.sync

import com.dashlane.sync.repositories.SyncProgress
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel



@Suppress("EXPERIMENTAL_API_USAGE")
object DataSyncStatus {

    val progress: BroadcastChannel<SyncProgress> =
        ConflatedBroadcastChannel()
}
