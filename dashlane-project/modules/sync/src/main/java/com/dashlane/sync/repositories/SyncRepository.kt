package com.dashlane.sync.repositories

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.sync.vault.SyncVault
import java.time.Duration
import java.time.Instant



interface SyncRepository {

    

    @Throws(SyncException::class)
    suspend fun sync(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        vault: SyncVault,
        syncProgressChannel: SyncProgressChannel? = null
    ): Result

    @Throws(SyncException::class)
    suspend fun syncChronological(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        vault: SyncVault
    )

    data class Timing(
        val start: Instant,
        val end: Instant
    ) {
        val duration: Duration
            get() = Duration.between(start, end)
    }

    data class Timings(
        val sync: Timing,
        val chronological: Timing?,
        val treatProblem: Timing?,
        val sharing: Timing?
    )

    data class Result(
        val transactionErrors: List<Throwable>,
        val timings: Timings,
        val statistics: Statistics,
        val treatProblemType: TreatProblemType,
        val duplicateCount: Int
    ) {

        data class Statistics(
            val outgoingTransactions: Outgoing,
            val incomingTransactions: Incoming
        ) {
            data class Outgoing(
                val updateCount: Int,
                val deleteCount: Int
            )

            

            data class Incoming(
                val updateCount: Int,
                val deleteCount: Int
            )
        }

        enum class TreatProblemType {
            NONE,
            DOWNLOAD,
            UPLOAD,
            SYNC
        }
    }

    class SyncException(
        val step: Step,
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message, cause) {
        var timings: Timings? = null

        enum class Step {
            CHRONOLOGICAL,
            DEDUPLICATION,
            TREAT,
            SHARING
        }
    }
}