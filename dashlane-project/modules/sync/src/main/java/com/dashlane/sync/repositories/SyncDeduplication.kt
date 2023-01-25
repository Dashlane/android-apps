package com.dashlane.sync.repositories

import com.dashlane.sync.vault.SyncVault



interface SyncDeduplication {
    

    suspend fun performDeduplication(vault: SyncVault): Int
}