package com.dashlane.sync.repositories

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.server.api.endpoints.account.SharingKeys
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.server.api.endpoints.sync.SyncUploadService
import com.dashlane.server.api.time.InstantEpochMilli
import com.dashlane.server.api.time.toInstant
import com.dashlane.server.api.time.toInstantEpochMilli
import com.dashlane.sync.domain.IncomingTransaction
import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.domain.TransactionCipher
import com.dashlane.sync.merger.SyncMerger
import com.dashlane.sync.repositories.strategies.SyncServices
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.sync.util.SyncLogs
import com.dashlane.sync.vault.SyncVault
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import javax.inject.Inject
import kotlin.reflect.KClass

class ChronologicalSyncImpl @Inject constructor(
    private val syncServices: SyncServices,
    private val syncMerger: SyncMerger,
    private val transactionCipher: TransactionCipher,
    private val incomingTransactionsHelper: IncomingTransactionsHelper,
    private val sharingSyncHelper: SharingSyncHelper,
    private val syncLogs: SyncLogs
) : ChronologicalSync {

    override suspend fun sync(
        serverCredentials: ServerCredentials,
        vault: SyncVault,
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncProgressChannel: SyncProgressChannel?
    ): ChronologicalSync.Result {
        val lastLocalSyncDate = vault.lastSyncTime

        
        val syncObjectClasses = enumValues<SyncObjectType>().map(SyncObjectType::kClass)
        vault.prepareOutgoingOperations(syncObjectClasses)
        val outgoingTransactions = vault.getOutgoingTransactions(syncObjectClasses)

        val outgoingUpdateCount = outgoingTransactions.count { it is OutgoingTransaction.Update }
        val outgoingDeleteCount = outgoingTransactions.count { it is OutgoingTransaction.Delete }

        syncLogs.onSyncChronologicalStart(
            lastLocalSyncDate,
            outgoingTransactions.count { it is OutgoingTransaction.Update },
            outgoingTransactions.count { it is OutgoingTransaction.Delete }
        )

        syncProgressChannel?.trySend(SyncProgress.RemoteSync)
        val (vaultOperations, backupDate, summary, sharingSummary, sharingKeys) =
            syncRemote(
                serverCredentials,
                sharingSyncHelper.shouldRequestSharingKeys,
                lastLocalSyncDate ?: Instant.EPOCH,
                cryptographyEngineFactory,
                vault,
                outgoingTransactions,
                syncProgressChannel
            )

        
        syncLocal(vault, vaultOperations.transactions, syncProgressChannel, syncObjectClasses)

        
        vault.lastSyncTime = backupDate

        syncLogs.onSyncChronologicalDone()

        return ChronologicalSync.Result(
            incomingTransactionErrors = vaultOperations.transactionErrors,
            summary = summary.toSummaryItems(),
            sharingSummary = sharingSummary,
            downloadedSharingKeys = sharingKeys,
            statistics = SyncRepository.Result.Statistics(
                outgoingTransactions = SyncRepository.Result.Statistics.Outgoing(
                    outgoingUpdateCount,
                    outgoingDeleteCount
                ),
                incomingTransactions = vaultOperations.statistics
            )
        )
    }

    

    private suspend fun syncRemote(
        serverCredentials: ServerCredentials,
        requestSharingKeys: Boolean,
        time: Instant,
        cryptographyEngineFactory: CryptographyEngineFactory,
        vault: SyncVault,
        pendingOperations: List<OutgoingTransaction>,
        syncProgressChannel: SyncProgressChannel?
    ): RemoteSyncResult {
        val downloadRequest = SyncDownloadService.Request(
            teamAdminGroups = false,
            needsKeys = requestSharingKeys,
            transactions = emptyList(),
            timestamp = time.toInstantEpochMilli()
        )
        val downloadData = syncServices.download(serverCredentials, downloadRequest)

        val incomingTransactions = incomingTransactionsHelper.readTransactions(
            downloadData,
            cryptographyEngineFactory,
            syncProgressChannel,
            downloadData.sharing.toSharedIds()
        )

        val isSharingKeyGenerationNeeded = downloadRequest.needsKeys && downloadData.sharingKeys == null
        val isUploadNeeded = pendingOperations.isNotEmpty() || isSharingKeyGenerationNeeded
        val sharingSummary = downloadData.sharing
        return if (isUploadNeeded) {
            upload(
                syncProgressChannel,
                incomingTransactions,
                pendingOperations,
                cryptographyEngineFactory,
                isSharingKeyGenerationNeeded,
                downloadData,
                serverCredentials,
                vault
            )
        } else {
            RemoteSyncResult(
                incomingTransactions,
                downloadData.timestamp.toInstant(),
                downloadData.summary,
                sharingSummary,
                downloadData.sharingKeys
            )
        }
    }

    private suspend fun upload(
        syncProgressChannel: SyncProgressChannel?,
        incomingTransactions: IncomingTransactionsHelper.Result,
        pendingOperations: List<OutgoingTransaction>,
        cryptographyEngineFactory: CryptographyEngineFactory,
        isSharingKeyGenerationNeeded: Boolean,
        downloadData: SyncDownloadService.Data,
        serverCredentials: ServerCredentials,
        vault: SyncVault
    ): RemoteSyncResult {
        syncProgressChannel?.trySend(SyncProgress.Upload)

        
        val (mergedOutgoingTransactions, mergedIncomingTransactions) = syncMerger.mergeRemoteAndLocalData(
            incomingTransactions = incomingTransactions.transactions,
            outgoingTransactions = pendingOperations
        )

        
        val uploadTransactions = transactionCipher.cipherOutgoingTransactions(
            transactions = mergedOutgoingTransactions,
            cryptographyEngineFactory = cryptographyEngineFactory
        )

        val sharingKeys = if (isSharingKeyGenerationNeeded) {
            sharingSyncHelper.generateSharingKeys()
        } else {
            null
        }

        val uploadRequest = SyncUploadService.Request(
            transactions = uploadTransactions,
            sharingKeys = sharingKeys?.let { (publicKey, privateKey) ->
                val encryptedPrivateKey = cryptographyEngineFactory.createEncryptionEngine().use { encryptionEngine ->
                    sharingSyncHelper.encryptPrivateKey(privateKey.value, encryptionEngine)
                }
                SharingKeys(
                    publicKey = publicKey.value,
                    privateKey = encryptedPrivateKey.value
                )
            },
            timestamp = downloadData.timestamp
        )

        
        val uploadData = syncServices.upload(
            serverCredentials,
            uploadRequest
        )

        sharingKeys?.let(sharingSyncHelper::saveSharingKeys)

        val uploadedUUIDS = mergedOutgoingTransactions.map { it.syncObjectType.kClass to it.identifier }
        vault.applyBackupDate(uploadedUUIDS, uploadData.timestamp.toInstant())

        return RemoteSyncResult(
            incomingTransactions.copy(transactions = mergedIncomingTransactions),
            uploadData.timestamp.toInstant(),
            uploadData.summary,
            downloadData.sharing,
            downloadData.sharingKeys
        )
    }

    private data class RemoteSyncResult(
        val incomingTransactions: IncomingTransactionsHelper.Result,
        val backupTime: Instant,
        val summary: Map<String, Map<String, InstantEpochMilli>>,
        val sharingSyncSummaryJson: SyncDownloadService.Data.SharingSummary,
        val downloadedSharingKeys: SharingKeys?
    )

    

    private suspend fun syncLocal(
        vault: SyncVault,
        vaultOperations: List<IncomingTransaction>,
        syncProgressChannel: SyncProgressChannel?,
        syncObjectClasses: List<KClass<out SyncObject>>
    ) {
        
        vault.inTransaction {
            syncLocal(vaultOperations, syncProgressChannel)
        }

        
        vault.clearOutgoingOperations(syncObjectClasses)
    }

    private fun SyncVault.TransactionScope.syncLocal(
        incomingTransactions: List<IncomingTransaction>,
        syncProgressChannel: SyncProgressChannel?
    ) {
        val operationCount = incomingTransactions.size
        incomingTransactions.forEachIndexed { index, it ->
            syncProgressChannel?.trySend(SyncProgress.LocalSync(index, operationCount))
            when (it) {
                is IncomingTransaction.Update -> insertOrUpdateForSync(
                    it.identifier,
                    it.syncObject,
                    it.date.toEpochMilli()
                )
                is IncomingTransaction.Delete -> deleteForSync(
                    it.syncObjectType.kClass,
                    it.identifier
                )
            }
        }
    }
}

private fun Map<String, Map<String, InstantEpochMilli>>.toSummaryItems(): List<SyncSummaryItem> =
    flatMap { (type, items) ->
        val syncObjectType = SyncObjectType.forTransactionTypeOrNull(type)
        if (syncObjectType == null) {
            emptyList()
        } else {
            items.map { (id, timestamp) ->
                SyncSummaryItem(id, timestamp.toInstant(), syncObjectType)
            }
        }
    }

internal fun SyncDownloadService.Data.SharingSummary.toSharedIds(): Set<String> =
    items.mapTo(mutableSetOf()) { it.id }
