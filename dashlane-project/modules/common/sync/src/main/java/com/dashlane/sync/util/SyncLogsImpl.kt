package com.dashlane.sync.util

import com.dashlane.sync.domain.Transaction
import com.dashlane.sync.treat.SyncSummaryItem
import java.time.Instant
import javax.inject.Inject

@Suppress("EXPERIMENTAL_API_USAGE")
class SyncLogsImpl @Inject constructor() : SyncLogs {
    override fun onSyncBegin() = info(message = "Start Type=Full", tag = Tag.SYNC)

    override fun onSyncChronologicalStart(
        syncTime: Instant?,
        updateCount: Int,
        deleteCount: Int,
    ) = info(
        message = "Start Timestamp: $syncTime Update=$updateCount Delete=$deleteCount",
        tag = Tag.SYNC_CHRONO,
    )

    override fun onDownloadRequest(
        syncTime: Instant?,
        lock: Boolean,
        sharingKeys: Boolean,
    ) = debug("Request Host=API Timestamp=$syncTime Lock=$lock SharingKeys=$sharingKeys", Tag.SYNC_DOWNLOAD)

    override fun onDownloadResponse(
        syncTime: Instant?,
        updateCount: Int?,
        deleteCount: Int?,
    ) = debug(message = "Success Timestamp=$syncTime Update=$updateCount Delete=$deleteCount", tag = Tag.SYNC_DOWNLOAD)

    override fun onDownloadError(throwable: Throwable) =
        warn(
            message = "Error Cause=${throwable::class.java.simpleName}",
            tag = Tag.SYNC_DOWNLOAD,
            throwable = throwable,
        )

    override fun onUploadRequest(updateCount: Int, deleteCount: Int) =
        debug(message = "Request Host=API Update=$updateCount Delete=$deleteCount", tag = Tag.SYNC_UPLOAD)

    override fun onUploadResponse(syncTime: Instant?) =
        debug(message = "Success Timestamp=$syncTime", tag = Tag.SYNC_UPLOAD)

    override fun onUploadError(throwable: Throwable) =
        debug(message = "Error ${throwable::class.java.simpleName}", tag = Tag.SYNC_UPLOAD)

    override fun onDecipherTransactionsStart() = verbose(message = "Start", tag = Tag.SYNC_DECIPHER_TRANSACTIONS)

    override fun onDecipherTransaction(
        action: String,
        type: String,
        identifier: String,
        date: Instant,
    ) = verbose(
        message = "Transaction Action=$action Type=$type Id=$identifier Date=$date",
        tag = Tag.SYNC_DECIPHER_TRANSACTIONS,
    )

    override fun onDecipherTransactionError(
        type: String,
        transaction: Transaction,
        throwable: Throwable,
    ) = warn(
        message = "Error Type=$type Id=${transaction.identifier} Date=${transaction.date} Cause=${throwable::class.java}",
        tag = Tag.SYNC_DECIPHER_TRANSACTIONS,
        throwable = throwable,
    )

    override fun onDecipherTransactionsDone(count: Int, errors: Int) =
        debug(message = "Done Transactions=$count Errors=$errors", tag = Tag.SYNC_DECIPHER_TRANSACTIONS)

    override fun onCipherTransactionsStart() = verbose(message = "Start", tag = Tag.SYNC_CIPHER_TRANSACTIONS)

    override fun onCipherTransaction(action: String, type: String, identifier: String, date: Instant) =
        verbose(message = "Transaction Type=$type Id=$identifier Date=$date", tag = Tag.SYNC_CIPHER_TRANSACTIONS)

    override fun onCipherTransactionsDone() = debug(message = "Done", tag = Tag.SYNC_CIPHER_TRANSACTIONS)

    override fun onSyncChronologicalDone() = debug(message = "Done", tag = Tag.SYNC_CHRONO)

    override fun onTreatProblemStart() = debug(message = "Start", tag = Tag.SYNC_TREAT_PROBLEM)

    override fun onTreatProblemSummaryBegin(localCount: Int, remoteCount: Int) =
        debug(message = "Start Local=$localCount Remote=$remoteCount", tag = Tag.SYNC_TREAT_PROBLEM_SUMMARY)

    override fun onTreatProblemSummaryUpToDate(item: List<SyncSummaryItem>) = verbose(
        message = "Comparison Result=UpToDate ${item.toDetailMessage()}",
        tag = Tag.SYNC_TREAT_PROBLEM_SUMMARY,
    )

    override fun onTreatProblemSummaryUploadMissing(item: List<SyncSummaryItem>) = verbose(
        message = "Comparison Result=Upload Cause=Missing ${item.toDetailMessage()}",
        tag = Tag.SYNC_TREAT_PROBLEM_SUMMARY,
    )

    override fun onTreatProblemSummaryUploadOutOfDate(item: List<SyncSummaryItem>) = verbose(
        message = "Comparison Result=Upload Cause=OutOfDate ${item.toDetailMessage()}",
        tag = Tag.SYNC_TREAT_PROBLEM_SUMMARY,
    )

    override fun onTreatProblemSummaryDownloadMissing(item: List<SyncSummaryItem>) = debug(
        message = "Comparison Result=Download Cause=Missing ${item.toDetailMessage()}",
        tag = Tag.SYNC_TREAT_PROBLEM_SUMMARY,
    )

    override fun onTreatProblemSummaryDownloadOutOfDate(item: List<SyncSummaryItem>) =
        debug(
            message = "Comparison Result=Download Cause=OutOfDate ${item.toDetailMessage()}",
            tag = Tag.SYNC_TREAT_PROBLEM_SUMMARY,
        )

    override fun onTreatProblemSummaryDone() =
        verbose(message = "Done", tag = Tag.SYNC_TREAT_PROBLEM_SUMMARY)

    override fun onTreatProblemNotNeeded() =
        info(message = "Diff UpToDate, TreatProblem not needed", tag = Tag.SYNC_TREAT_PROBLEM)

    override fun onTreatProblemUpload(uploadCount: Int) =
        warn(message = "Diff Upload=$uploadCount", tag = Tag.SYNC_TREAT_PROBLEM)

    override fun onTreatProblemDownload(downloadCount: Int) =
        warn(message = "Diff Download=$downloadCount", tag = Tag.SYNC_TREAT_PROBLEM)

    override fun onTreatProblemApplyDownloadedTransactionsLocally(t: Throwable) {
        warn(
            message = "onTreatProblemApplyDownloadedTransactionsLocally",
            tag = Tag.SYNC_TREAT_PROBLEM,
            throwable = t,
        )
    }

    override fun onTreatProblemDone() = info(message = "Done", tag = Tag.SYNC_TREAT_PROBLEM)

    override fun onTreatProblemDownloadEmpty() = warn(message = "Downloaded transactions are empty", tag = Tag.SYNC_TREAT_PROBLEM)

    override fun onTreatProblemDownloadMissed() =
        warn(message = "Downloaded transactions doesn't match requested", tag = Tag.SYNC_TREAT_PROBLEM)

    override fun onSyncDone() = info(message = "Done", tag = Tag.SYNC)

    override fun onSyncError(t: Throwable) = error(message = "Failed.", tag = Tag.SYNC, throwable = t)

    private fun List<SyncSummaryItem>.toDetailMessage() = if (isEmpty()) {
        "Empty"
    } else {
        groupBy { it.syncObjectType }
            .map { (key, value) -> "$key=${value.size}" }
            .joinToString()
    }

    private object Tag {
        const val SYNC = "Sync"
        const val SYNC_CHRONO = "$SYNC|Chrono"
        const val SYNC_DOWNLOAD = "$SYNC|Download"
        const val SYNC_UPLOAD = "$SYNC|Upload"
        const val SYNC_DECIPHER_TRANSACTIONS = "$SYNC|Decipher|Transactions"
        const val SYNC_CIPHER_TRANSACTIONS = "$SYNC|Cipher|Transactions"
        const val SYNC_TREAT_PROBLEM = "$SYNC|TreatProblem"
        const val SYNC_TREAT_PROBLEM_SUMMARY = "$SYNC|TreatProblem|Summary"
    }
}