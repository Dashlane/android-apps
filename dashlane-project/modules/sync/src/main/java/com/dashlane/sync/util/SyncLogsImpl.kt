package com.dashlane.sync.util

import com.dashlane.sync.domain.Transaction
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import javax.inject.Inject

@Suppress("EXPERIMENTAL_API_USAGE")
class SyncLogsImpl @Inject constructor(private val writer: SyncLogsWriter) : SyncLogs {

    override fun onSyncBegin() =
        info(Tag.SYNC) { "Start Type=Full" }

    override fun onSyncChronologicalStart(
        syncTime: Instant?,
        updateCount: Int,
        deleteCount: Int
    ) =
        info(Tag.SYNC_CHRONO) { "Start Timestamp: $syncTime Update=$updateCount Delete=$deleteCount" }

    override fun onDownloadRequest(
        syncTime: Instant?,
        lock: Boolean,
        sharingKeys: Boolean
    ) =
        debug(Tag.SYNC_DOWNLOAD) { "Request Host=API Timestamp=$syncTime Lock=$lock SharingKeys=$sharingKeys" }

    override fun onDownloadResponse(
        syncTime: Instant?,
        updateCount: Int?,
        deleteCount: Int?
    ) =
        debug(Tag.SYNC_DOWNLOAD) {
            "Success Timestamp=$syncTime Update=$updateCount Delete=$deleteCount"
        }

    override fun onDownloadError(throwable: Throwable) =
        warn(Tag.SYNC_DOWNLOAD, throwable) { "Error Cause=${throwable::class.java.simpleName}" }

    override fun onUploadRequest(updateCount: Int, deleteCount: Int) =
        debug(Tag.SYNC_UPLOAD) { "Request Host=API Update=$updateCount Delete=$deleteCount" }

    override fun onUploadResponse(syncTime: Instant?) =
        debug(Tag.SYNC_UPLOAD) { "Success Timestamp=$syncTime" }

    override fun onUploadError(throwable: Throwable) =
        debug(Tag.SYNC_UPLOAD) { "Error ${throwable::class.java.simpleName}" }

    override fun onDecipherTransactionsStart() =
        verbose(Tag.SYNC_DECIPHER_TRANSACTIONS) { "Start" }

    override fun onDecipherTransaction(
        action: String,
        type: String,
        identifier: String,
        date: Instant
    ) =
        verbose(Tag.SYNC_DECIPHER_TRANSACTIONS) { "Transaction Action=$action Type=$type Id=$identifier Date=$date" }

    override fun onDecipherTransactionError(
        type: String,
        transaction: Transaction,
        throwable: Throwable
    ) =
        warn(Tag.SYNC_DECIPHER_TRANSACTIONS, throwable) {
            "Error Type=$type Id=${transaction.identifier} Date=${transaction.date} Cause=${throwable::class.java}"
        }

    override fun onDecipherTransactionsDone(count: Int, errors: Int) =
        debug(Tag.SYNC_DECIPHER_TRANSACTIONS) { "Done Transactions=$count Errors=$errors" }

    override fun onCipherTransactionsStart() =
        verbose(Tag.SYNC_CIPHER_TRANSACTIONS) { "Start" }

    override fun onCipherTransaction(
        action: String,
        type: String,
        identifier: String,
        date: Instant
    ) =
        verbose(Tag.SYNC_CIPHER_TRANSACTIONS) { "Transaction Type=$type Id=$identifier Date=$date" }

    override fun onCipherTransactionsDone() =
        debug(Tag.SYNC_CIPHER_TRANSACTIONS) { "Done" }

    override fun onSyncChronologicalDone() =
        debug(Tag.SYNC_CHRONO) { "Done" }

    override fun onTreatProblemStart() =
        debug(Tag.SYNC_TREAT_PROBLEM) { "Start" }

    override fun onTreatProblemSummaryBegin(localCount: Int, remoteCount: Int) =
        debug(Tag.SYNC_TREAT_PROBLEM_SUMMARY) { "Start Local=$localCount Remote=$remoteCount" }

    override fun onTreatProblemSummaryUpToDate(type: SyncObjectType, identifier: String, backupTime: Instant?) =
        verbose(Tag.SYNC_TREAT_PROBLEM_SUMMARY) {
            "Comparison Result=UpToDate Type=$type Id=$identifier Date=$backupTime"
        }

    override fun onTreatProblemSummaryUploadMissing(
        type: SyncObjectType,
        identifier: String,
        backupTime: Instant?
    ) =
        verbose(Tag.SYNC_TREAT_PROBLEM_SUMMARY) {
            "Comparison Result=Upload Cause=Missing Type=$type Id=$identifier Date=$backupTime"
        }

    override fun onTreatProblemSummaryUploadOutOfDate(
        type: SyncObjectType,
        identifier: String,
        backupTime: Instant?,
        outOfDateTime: Instant?
    ) =
        verbose(Tag.SYNC_TREAT_PROBLEM_SUMMARY) {
            "Comparison Result=Upload Cause=OutOfDate Type=$type Id=$identifier Date=$backupTime"
        }

    override fun onTreatProblemSummaryDownloadMissing(
        type: SyncObjectType,
        identifier: String,
        backupTime: Instant?
    ) =
        debug(Tag.SYNC_TREAT_PROBLEM_SUMMARY) {
            "Comparison Result=Download Cause=Missing Type=$type Id=$identifier Date=$backupTime"
        }

    override fun onTreatProblemSummaryDownloadOutOfDate(
        type: SyncObjectType,
        identifier: String,
        backupTime: Instant?,
        outOfDateTime: Instant?
    ) =
        debug(Tag.SYNC_TREAT_PROBLEM_SUMMARY) {
            "Comparison Result=Download Cause=OutOfDate Type=$type Id=$identifier Date=$backupTime"
        }

    override fun onTreatProblemSummaryDone() =
        verbose(Tag.SYNC_TREAT_PROBLEM_SUMMARY) { "Done" }

    override fun onTreatProblemNotNeeded() =
        info(Tag.SYNC_TREAT_PROBLEM) { "Diff UpToDate" }

    override fun onTreatProblemUpload(downloadCount: Int, uploadCount: Int) =
        warn(Tag.SYNC_TREAT_PROBLEM) { "Diff Upload=$uploadCount" }

    override fun onTreatProblemDone() =
        info(Tag.SYNC_TREAT_PROBLEM) { "Done" }

    override fun onSyncDone() =
        info(Tag.SYNC) { "Done" }

    override fun onSyncError(t: Throwable) =
        error(Tag.SYNC, t) { "Failed." }

    private inline fun verbose(tag: String, throwable: Throwable? = null, lazyMessage: () -> String) =
        writer.verbose(tag, throwable, lazyMessage())

    private inline fun debug(tag: String, throwable: Throwable? = null, lazyMessage: () -> String) =
        writer.debug(tag, throwable, lazyMessage())

    private inline fun info(tag: String, throwable: Throwable? = null, lazyMessage: () -> String) =
        writer.info(tag, throwable, lazyMessage())

    private inline fun warn(tag: String, throwable: Throwable? = null, lazyMessage: () -> String) =
        writer.warn(tag, throwable, lazyMessage())

    private inline fun error(tag: String, throwable: Throwable? = null, lazyMessage: () -> String) =
        writer.error(tag, throwable, lazyMessage())

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