package com.dashlane.sync.repositories

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.server.api.endpoints.account.SharingKeys
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.sync.DataSyncState
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.sync.treat.TreatProblemManager
import com.dashlane.sync.util.SyncLogs
import com.dashlane.sync.vault.SyncVault
import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.Instant
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val chronologicalSync: ChronologicalSync,
    private val deduplication: SyncDeduplication,
    private val treatProblemManager: TreatProblemManager,
    private val sharingSyncHelper: SharingSyncHelper,
    private val syncLogs: SyncLogs
) : SyncRepository {

    override suspend fun sync(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        vault: SyncVault,
        dataSyncState: MutableSharedFlow<DataSyncState>?,
    ): SyncRepository.Result {
        syncLogs.onSyncBegin()

        try {
            return syncVault(
                serverCredentials = serverCredentials,
                cryptographyEngineFactory = cryptographyEngineFactory,
                vault = vault,
                dataSyncState = dataSyncState
            )
        } catch (t: Throwable) {
            
            syncLogs.onSyncError(t)
            throw t
        } finally {
            syncLogs.onSyncDone()
        }
    }

    override suspend fun syncChronological(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        vault: SyncVault
    ) {
        syncLogs.onSyncBegin()
        try {
            runChronologicalSync(
                serverCredentials,
                cryptographyEngineFactory,
                vault,
                null
            )
        } catch (t: Throwable) {
            
            syncLogs.onSyncError(t)
            throw t
        } finally {
            syncLogs.onSyncDone()
        }
    }

    private suspend fun syncVault(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        vault: SyncVault,
        dataSyncState: MutableSharedFlow<DataSyncState>?,
    ): SyncRepository.Result {
        val syncStart = Instant.now()
        var chronologicalSyncEnd: Instant? = null
        var treatProblemEnd: Instant? = null
        var sharingSyncEnd: Instant? = null

        fun timings() = SyncRepository.Timings(
            sync = SyncRepository.Timing(start = syncStart, end = Instant.now()),
            chronological = chronologicalSyncEnd?.let { SyncRepository.Timing(start = syncStart, it) },
            treatProblem = treatProblemEnd?.let { SyncRepository.Timing(start = chronologicalSyncEnd!!, it) },
            sharing = sharingSyncEnd?.let { SyncRepository.Timing(start = treatProblemEnd!!, it) }
        )

        try {
            
            val chronologicalSyncResult = runChronologicalSync(
                serverCredentials = serverCredentials,
                cryptographyEngineFactory = cryptographyEngineFactory,
                vault = vault,
                dataSyncState = dataSyncState
            )

            chronologicalSyncEnd = Instant.now()

            
            val treatProblemResult = runTreatProblem(
                serverCredentials = serverCredentials,
                cryptographyEngineFactory = cryptographyEngineFactory,
                vault = vault,
                dataSyncState = dataSyncState,
                summary = chronologicalSyncResult.summary
            )

            treatProblemEnd = Instant.now()

            
            runSharingSync(
                serverCredentials = serverCredentials,
                cryptographyEngineFactory = cryptographyEngineFactory,
                dataSyncState = dataSyncState,
                sharingKeys = chronologicalSyncResult.downloadedSharingKeys,
                sharingSummary = chronologicalSyncResult.sharingSummary
            )

            sharingSyncEnd = Instant.now()

            
            val duplicateCount = runDeduplication(vault)

            return SyncRepository.Result(
                transactionErrors = chronologicalSyncResult.incomingTransactionErrors + treatProblemResult.transactionErrors,
                timings = timings(),
                statistics = chronologicalSyncResult.statistics,
                treatProblemType = treatProblemResult.type,
                duplicateCount = duplicateCount
            )
        } catch (e: SyncRepository.SyncException) {
            e.timings = timings()
            throw e
        }
    }

    private suspend fun runChronologicalSync(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        vault: SyncVault,
        dataSyncState: MutableSharedFlow<DataSyncState>?,
    ): ChronologicalSync.Result =
        runSyncStep(SyncRepository.SyncException.Step.CHRONOLOGICAL) {
            chronologicalSync.sync(
                serverCredentials = serverCredentials,
                vault = vault,
                cryptographyEngineFactory = cryptographyEngineFactory,
                dataSyncState = dataSyncState
            )
        }

    private suspend fun runTreatProblem(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        vault: SyncVault,
        dataSyncState: MutableSharedFlow<DataSyncState>?,
        summary: List<SyncSummaryItem>
    ): TreatProblemManager.Result =
        runSyncStep(SyncRepository.SyncException.Step.TREAT) {
            syncLogs.onTreatProblemStart()
            dataSyncState?.emit(DataSyncState.Active(SyncProgress.TreatProblem))
            treatProblemManager.execute(
                serverCredentials = serverCredentials,
                serverSummary = summary,
                syncVault = vault,
                cryptographyEngineFactory = cryptographyEngineFactory
            )
        }

    private suspend fun runSharingSync(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        dataSyncState: MutableSharedFlow<DataSyncState>?,
        sharingKeys: SharingKeys?,
        sharingSummary: SyncDownloadService.Data.SharingSummary
    ): Unit =
        runSyncStep(SyncRepository.SyncException.Step.SHARING) {
            sharingSyncHelper.syncSharing(
                serverCredentials = serverCredentials,
                sharingKeys = sharingKeys,
                sharingSummary = sharingSummary,
                cryptographyEngineFactory = cryptographyEngineFactory,
                dataSyncState = dataSyncState
            )
        }

    private suspend fun runDeduplication(
        vault: SyncVault
    ): Int =
        runSyncStep(SyncRepository.SyncException.Step.DEDUPLICATION) {
            deduplication.performDeduplication(vault)
        }

    private inline fun <T> runSyncStep(
        step: SyncRepository.SyncException.Step,
        block: () -> T
    ): T {
        return try {
            block()
        } catch (t: Throwable) {
            throw SyncRepository.SyncException(step, "Sync failed. ", t)
        }
    }
}
