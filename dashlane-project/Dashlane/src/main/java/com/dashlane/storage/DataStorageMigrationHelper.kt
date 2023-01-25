package com.dashlane.storage

import kotlinx.coroutines.CoroutineScope
import java.time.Instant



interface DataStorageMigrationHelper {
    

    suspend fun waiting()

    

    fun migration(coroutineScope: CoroutineScope, action: suspend () -> Unit)

    

    suspend fun localMigration()
    

    fun logSyncFailIfLegacyDatabase(e: Throwable, lastSyncTime: Instant)

    

    fun logShouldStartMigrationPreSync()

    

    fun logShouldStartMigrationPreCrypto()
}