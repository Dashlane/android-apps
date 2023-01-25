package com.dashlane.sync.repositories

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.server.api.endpoints.account.SharingKeys
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.sync.vault.SyncVault



interface ChronologicalSync {

    suspend fun sync(
        serverCredentials: ServerCredentials,
        vault: SyncVault,
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncProgressChannel: SyncProgressChannel?
    ): Result

    data class Result(
        val incomingTransactionErrors: List<Throwable>,
        val summary: List<SyncSummaryItem>,
        val sharingSummary: SyncDownloadService.Data.SharingSummary,
        val downloadedSharingKeys: SharingKeys?,
        val statistics: SyncRepository.Result.Statistics
    )
}
