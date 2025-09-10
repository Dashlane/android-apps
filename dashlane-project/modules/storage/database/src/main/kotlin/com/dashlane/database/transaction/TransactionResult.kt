package com.dashlane.database.transaction

import java.time.Duration
import java.time.Instant

data class TransactionResult(
    val timings: Timings,
    val statistics: Statistics,
)

data class Statistics(
    val summaryCount: Int?,
    val syncSummaryCount: Int?,
    val dataChangeHistorySummaryCount: Int?,
    val updateCount: Int,
    val deleteCount: Int,
)

data class Timings(
    val transaction: Timing,
    val writeItems: Timing,
    val writeBackups: Timing,
    val delete: Timing,
    val summary: Timing,
    val applyTransaction: Timing,
) {
    val all: List<Timing>
        get() = listOf(
            transaction,
            writeItems,
            writeBackups,
            delete,
            summary,
            applyTransaction,
        )

    data class Builder(
        var transactionStart: Instant? = null,
        var transactionEnd: Instant? = null,
        var writeItemsStart: Instant? = null,
        var writeItemsEnd: Instant? = null,
        var writeBackupsStart: Instant? = null,
        var writeBackupsEnd: Instant? = null,
        var deleteStart: Instant? = null,
        var deleteEnd: Instant? = null,
        var summaryStart: Instant? = null,
        var summaryEnd: Instant? = null,
        var applyTransactionStart: Instant? = null,
        var applyTransactionEnd: Instant? = null,
    ) {
        fun build(): Timings =
            Timings(
                transaction = Timing(transactionStart, transactionEnd),
                writeItems = Timing(writeItemsStart, writeItemsEnd),
                writeBackups = Timing(writeBackupsStart, writeBackupsEnd),
                delete = Timing(deleteStart, deleteEnd),
                summary = Timing(summaryStart, summaryEnd),
                applyTransaction = Timing(applyTransactionStart, applyTransactionEnd),
            )
    }
}

data class Timing(
    val start: Instant? = null,
    val end: Instant? = null,
) {
    val duration: Duration
        get() =
            if (start == null || end == null) {
                Duration.ZERO
            } else {
                Duration.between(start, end)
            }
}