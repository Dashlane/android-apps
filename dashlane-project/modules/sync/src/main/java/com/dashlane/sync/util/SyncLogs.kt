package com.dashlane.sync.util

import com.dashlane.sync.domain.Transaction
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant



interface SyncLogs : SyncNetworkLogs, SyncDecipherTransactionLogs,
    SyncCipherLogs, SyncTreatProblemLogs {

    

    fun onSyncBegin()

    

    fun onSyncChronologicalStart(
        syncTime: Instant?,
        updateCount: Int,
        deleteCount: Int
    )

    

    fun onSyncChronologicalDone()

    

    fun onSyncDone()

    

    fun onSyncError(t: Throwable)
}

interface SyncNetworkLogs {

    

    fun onDownloadRequest(
        syncTime: Instant?,
        lock: Boolean,
        sharingKeys: Boolean
    )

    

    fun onDownloadResponse(
        syncTime: Instant?,
        updateCount: Int?,
        deleteCount: Int?
    )

    

    fun onDownloadError(throwable: Throwable)

    fun onUploadRequest(updateCount: Int, deleteCount: Int)

    fun onUploadResponse(syncTime: Instant?)

    fun onUploadError(throwable: Throwable)
}



interface SyncDecipherTransactionLogs {
    fun onDecipherTransactionsStart()
    fun onDecipherTransaction(
        action: String,
        type: String,
        identifier: String,
        date: Instant
    )

    fun onDecipherTransactionError(
        type: String,
        transaction: Transaction,
        throwable: Throwable
    )

    fun onDecipherTransactionsDone(count: Int, errors: Int)
}



interface SyncCipherLogs {

    

    fun onCipherTransactionsStart()

    

    fun onCipherTransaction(
        action: String,
        type: String,
        identifier: String,
        date: Instant
    )

    fun onCipherTransactionsDone()
}



interface SyncTreatProblemLogs {

    fun onTreatProblemStart()
    fun onTreatProblemSummaryBegin(localCount: Int, remoteCount: Int)
    fun onTreatProblemSummaryUploadMissing(type: SyncObjectType, identifier: String, backupTime: Instant?)
    fun onTreatProblemSummaryUpToDate(type: SyncObjectType, identifier: String, backupTime: Instant?)
    fun onTreatProblemSummaryUploadOutOfDate(
        type: SyncObjectType,
        identifier: String,
        backupTime: Instant?,
        outOfDateTime: Instant?
    )

    fun onTreatProblemSummaryDownloadMissing(type: SyncObjectType, identifier: String, backupTime: Instant?)
    fun onTreatProblemSummaryDownloadOutOfDate(
        type: SyncObjectType,
        identifier: String,
        backupTime: Instant?,
        outOfDateTime: Instant?
    )

    fun onTreatProblemSummaryDone()

    

    fun onTreatProblemNotNeeded()

    

    fun onTreatProblemUpload(downloadCount: Int, uploadCount: Int)

    

    fun onTreatProblemDone()
}