package com.dashlane.sync

import com.dashlane.sync.util.SyncLogsWriter
import dagger.Module
import dagger.Provides

@Module
data class SyncLogsModule(
    @get:Provides
    val syncLogsWriter: SyncLogsWriter
)