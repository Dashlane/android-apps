package com.dashlane.sync.treat

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.server.api.endpoints.sync.SyncDownloadTransaction
import com.dashlane.server.api.endpoints.sync.SyncUploadService
import com.dashlane.server.api.endpoints.sync.SyncUploadTransaction
import com.dashlane.server.api.time.toInstant
import com.dashlane.server.api.time.toInstantEpochMilli
import com.dashlane.sync.domain.IncomingTransaction
import com.dashlane.sync.domain.TransactionCipher
import com.dashlane.sync.repositories.ServerCredentials
import com.dashlane.sync.repositories.SyncRepository.Result.TreatProblemType
import com.dashlane.sync.repositories.strategies.SyncServices
import com.dashlane.sync.repositories.toSharedIds
import com.dashlane.sync.util.SyncLogs
import com.dashlane.sync.vault.SyncVault
import com.dashlane.util.logV
import com.dashlane.util.logW
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import javax.inject.Inject

class TreatProblemManagerImpl @Inject constructor(
    private val syncServices: SyncServices,
    private val transactionCipher: TransactionCipher,
    private val transactionFailureRegistry: TransactionFailureRegistry,
    private val syncLogs: SyncLogs
) : TreatProblemManager {

    override suspend fun execute(
        serverCredentials: ServerCredentials,
        serverSummary: List<SyncSummaryItem>,
        syncVault: SyncVault,
        cryptographyEngineFactory: CryptographyEngineFactory
    ): TreatProblemManager.Result {
        val (itemsToUpload, uuidsToDownload) = getSyncSummaryDiff(serverSummary.toSet(), syncVault)

        val timestamp = syncVault.lastSyncTime ?: Instant.EPOCH

        
        val isDownloadNeeded = uuidsToDownload.isNotEmpty()
        val ignoredErrors = if (isDownloadNeeded) {
            val downloadRequest = SyncDownloadService.Request(
                teamAdminGroups = false,
                needsKeys = false,
                transactions = uuidsToDownload.toList(),
                timestamp = timestamp.toInstantEpochMilli()
            )
            treatProblemDownload(
                serverCredentials,
                downloadRequest,
                cryptographyEngineFactory,
                syncVault,
                uuidsToDownload
            )
        } else {
            emptyList()
        }

        val isUploadNeeded = itemsToUpload.isNotEmpty()
        if (isUploadNeeded) {
            treatProblemUpload(
                serverCredentials,
                cryptographyEngineFactory,
                syncVault,
                uuidsToDownload,
                itemsToUpload,
                timestamp
            )
        }

        val treatProblemType = when {
            isDownloadNeeded && isUploadNeeded -> TreatProblemType.SYNC
            isDownloadNeeded -> TreatProblemType.DOWNLOAD
            isUploadNeeded -> TreatProblemType.UPLOAD
            else -> {
                syncLogs.onTreatProblemNotNeeded()
                TreatProblemType.NONE
            }
        }

        syncLogs.onTreatProblemDone()

        return TreatProblemManager.Result(
            treatProblemType,
            ignoredErrors
        )
    }

    private suspend fun getSyncSummaryDiff(
        serverSummary: Set<SyncSummaryItem>,
        syncVault: SyncVault
    ): Pair<List<SyncSummaryItem>, Set<String>> {
        val syncObjectTypes = enumValues<SyncObjectType>().toList()
        val failedTransactions = transactionFailureRegistry.getAll()
        val failedTransactionIds = failedTransactions.map { it.objectId }.toSet()
        val localSummary = cleanupDuplicateIdsBug(
            syncVault,
            failedTransactions + syncVault.getSummary(syncObjectTypes).filterNot { it.objectId in failedTransactionIds }
        )

        syncLogs.onTreatProblemSummaryBegin(localSummary.size, serverSummary.size)

        val upToDateObjects = localSummary.intersect(serverSummary)
        upToDateObjects.forEach {
            syncLogs.onTreatProblemSummaryUpToDate(
                it.syncObjectType,
                it.objectId,
                it.lastUpdateTime
            )
        }
        val serverObjectById = (serverSummary - upToDateObjects).associateBy(SyncSummaryItem::objectId)
        val localSummaryById = (localSummary - upToDateObjects).associateBy(SyncSummaryItem::objectId)

        
        val itemsToUpload = localSummaryById.values.filter { localObject ->
            shouldUpload(localObject, serverObjectById[localObject.objectId])
        }

        
        val uuidsToDownload = serverObjectById.values
            .asSequence()
            .filter { shouldDownload(it, localSummaryById[it.objectId]) }
            .map(SyncSummaryItem::objectId)
            .toSet()

        syncLogs.onTreatProblemSummaryDone()
        return Pair(itemsToUpload, uuidsToDownload)
    }

    

    private suspend fun cleanupDuplicateIdsBug(
        syncVault: SyncVault,
        localSummary: Set<SyncSummaryItem>
    ): Set<SyncSummaryItem> {
        
        
        val ids = mutableSetOf<String>()
        val map = mutableMapOf<String, SyncSummaryItem>()
        localSummary.forEach {
            val key = it.objectId
            val item = map.put(key, it)
            if (item != null) {
                val isAnyDataChangeHistory = item.syncObjectType == SyncObjectType.DATA_CHANGE_HISTORY ||
                        it.syncObjectType == SyncObjectType.DATA_CHANGE_HISTORY
                if (isAnyDataChangeHistory) {
                    ids += key
                }
                
                
            }
        }
        return if (ids.isEmpty()) {
            localSummary
        } else {
            syncVault.inTransaction {
                ids.forEach {
                    deleteForSync(SyncObjectType.DATA_CHANGE_HISTORY.kClass, it)
                }
            }
            localSummary.filterNot { it.objectId in ids && it.syncObjectType == SyncObjectType.DATA_CHANGE_HISTORY }.toSet()
        }
    }

    private suspend fun treatProblemDownload(
        serverCredentials: ServerCredentials,
        downloadRequest: SyncDownloadService.Request,
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncVault: SyncVault,
        uuidsToDownload: Set<String>
    ): List<Throwable> {
        syncLogs.onTreatProblemUpload(uuidsToDownload.size, 0)

        val downloadData = syncServices.download(serverCredentials, downloadRequest)

        return applyDownloadedTransactionsLocally(cryptographyEngineFactory, syncVault, downloadData, uuidsToDownload, downloadData.sharing)
    }

    private suspend fun treatProblemUpload(
        serverCredentials: ServerCredentials,
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncVault: SyncVault,
        uuidsToDownload: Set<String>,
        itemsToUpload: List<SyncSummaryItem>,
        timestamp: Instant
    ) {
        syncLogs.onTreatProblemUpload(uuidsToDownload.size, itemsToUpload.size)

        val outgoingTransactions = createUploadTransactions(cryptographyEngineFactory, syncVault, itemsToUpload)
        val uploadRequest = SyncUploadService.Request(
            transactions = outgoingTransactions,
            timestamp = timestamp.toInstantEpochMilli()
        )

        val uploadData = syncServices.upload(serverCredentials, uploadRequest)

        applyUploadedTransactionsLocally(uploadData, syncVault, itemsToUpload)
    }

    private suspend fun createUploadTransactions(
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncVault: SyncVault,
        itemsToUpload: List<SyncSummaryItem>
    ): List<SyncUploadTransaction> {
        val batch = itemsToUpload.map { it.syncObjectType.kClass to it.objectId }
        val outgoingTransactions = syncVault.fetchAsOutgoingUpdate(batch)

        return transactionCipher.cipherOutgoingTransactions(
            transactions = outgoingTransactions,
            cryptographyEngineFactory = cryptographyEngineFactory
        )
    }

    private suspend fun applyUploadedTransactionsLocally(
        uploaded: SyncUploadService.Data,
        syncVault: SyncVault,
        itemsToUpload: List<SyncSummaryItem>
    ) {
        logV { "Upload $uploaded" }

        val latestTimestamp = uploaded.timestamp.toInstant()
        syncVault.lastSyncTime = latestTimestamp
        syncVault.applyBackupDate(
            itemsToUpload.map { it.syncObjectType.kClass to it.objectId },
            latestTimestamp
        )
    }

    private suspend fun applyDownloadedTransactionsLocally(
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncVault: SyncVault,
        latestResult: SyncDownloadService.Data,
        uuidsToDownload: Set<String>,
        sharing: SyncDownloadService.Data.SharingSummary
    ): List<Throwable> {
        val transactionList = latestResult.transactions
        val usefulTransactions =
            transactionList.filter { uuidsToDownload.contains(it.identifier) } 
        if (usefulTransactions.size < uuidsToDownload.size) {
            logW {
                "Server didn't return requested transactions: " +
                        (uuidsToDownload - usefulTransactions.map(SyncDownloadTransaction::identifier)).toString()
            }
        }

        if (transactionList.isEmpty()) return emptyList()
        val (incomingTransactions, ignoredErrors) = transactionCipher.decipherIncomingTransactions(
            transactionList,
            cryptographyEngineFactory,
            sharing.toSharedIds()
        )

        
        syncVault.inTransaction {
            for (incomingTransaction in incomingTransactions) {
                when (incomingTransaction) {
                    is IncomingTransaction.Update -> insertOrUpdateForSync(
                        incomingTransaction.identifier,
                        incomingTransaction.syncObject,
                        incomingTransaction.date.toEpochMilli()
                    )
                    is IncomingTransaction.Delete -> deleteForSync(
                        incomingTransaction.syncObjectType.kClass,
                        incomingTransaction.identifier
                    )
                }
            }
        }

        return ignoredErrors
    }

    private fun shouldUpload(localObject: SyncSummaryItem, serverObject: SyncSummaryItem?): Boolean {
        if (serverObject == null) {
            syncLogs.onTreatProblemSummaryUploadMissing(
                localObject.syncObjectType,
                localObject.objectId,
                localObject.lastUpdateTime
            )
            return true
        }
        if (serverObject.lastUpdateTime < localObject.lastUpdateTime) {
            syncLogs.onTreatProblemSummaryUploadOutOfDate(
                localObject.syncObjectType,
                localObject.objectId,
                localObject.lastUpdateTime,
                serverObject.lastUpdateTime
            )
            return true
        }
        return false
    }

    private fun shouldDownload(serverObject: SyncSummaryItem, localObject: SyncSummaryItem?): Boolean {
        if (localObject == null) {
            syncLogs.onTreatProblemSummaryDownloadMissing(
                serverObject.syncObjectType,
                serverObject.objectId,
                serverObject.lastUpdateTime
            )
            return true
        }
        if (localObject.lastUpdateTime < serverObject.lastUpdateTime) {
            syncLogs.onTreatProblemSummaryDownloadOutOfDate(
                serverObject.syncObjectType,
                serverObject.objectId,
                serverObject.lastUpdateTime,
                localObject.lastUpdateTime
            )
            return true
        }
        return false
    }
}
