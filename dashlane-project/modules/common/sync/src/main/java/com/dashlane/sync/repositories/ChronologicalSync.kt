package com.dashlane.sync.repositories

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.server.api.endpoints.account.SharingKeys
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.sync.DataSyncState
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.sync.vault.SyncVault
import kotlinx.coroutines.flow.MutableSharedFlow

interface ChronologicalSync {

    suspend fun sync(
        serverCredentials: ServerCredentials,
        vault: SyncVault,
        cryptographyEngineFactory: CryptographyEngineFactory,
        dataSyncState: MutableSharedFlow<DataSyncState>?,
    ): Result

    data class Result(
        val incomingTransactionErrors: List<Throwable>,
        val summary: List<SyncSummaryItem>,
        val sharingSummary: SyncDownloadService.Data.SharingSummary,
        val downloadedSharingKeys: SharingKeys?,
        val statistics: SyncRepository.Result.Statistics
    )
}
