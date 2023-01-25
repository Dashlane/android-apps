package com.dashlane.sync.domain

import com.dashlane.cryptography.CryptographyBase64Exception
import com.dashlane.cryptography.CryptographyEngineException
import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.cryptography.CryptographyException
import com.dashlane.cryptography.XmlDecryptionEngine
import com.dashlane.cryptography.XmlEncryptionEngine
import com.dashlane.cryptography.asEncryptedBase64
import com.dashlane.cryptography.decryptBase64ToXmlTransaction
import com.dashlane.cryptography.encryptXmlTransactionToBase64String
import com.dashlane.cryptography.forXml
import com.dashlane.server.api.endpoints.sync.SyncDownloadTransaction
import com.dashlane.server.api.endpoints.sync.SyncUploadTransaction
import com.dashlane.server.api.time.toInstant
import com.dashlane.server.api.time.toInstantEpochSecond
import com.dashlane.sync.treat.TransactionFailureRegistry
import com.dashlane.sync.util.SyncLogs
import com.dashlane.sync.util.mapAsync
import com.dashlane.xml.XmlTypeException
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.copy
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import com.dashlane.xml.serializer.XmlException
import com.dashlane.xml.serializer.XmlSerialization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TransactionCipherImpl(
    private val xmlSerialization: XmlSerialization = XmlSerialization,
    private val transactionFailureRegistry: TransactionFailureRegistry,
    private val syncLogs: SyncLogs
) : TransactionCipher {

    @Inject
    constructor(
        syncLogs: SyncLogs,
        transactionFailureRegistry: TransactionFailureRegistry
    ) : this(
        XmlSerialization,
        transactionFailureRegistry,
        syncLogs
    )

    

    override suspend fun decipherIncomingTransactions(
        transactions: List<SyncDownloadTransaction>,
        cryptographyEngineFactory: CryptographyEngineFactory,
        sharedIds: Set<String>,
        progressChannel: SendChannel<Unit>?
    ) = withContext(Dispatchers.Default) {
        cryptographyEngineFactory.createDecryptionEngine().forXml(xmlSerialization).use { decryptionEngine ->
            decipherIncomingTransactions(transactions, progressChannel, decryptionEngine, sharedIds)
        }
    }

    private suspend fun decipherIncomingTransactions(
        transactions: List<SyncDownloadTransaction>,
        progressChannel: SendChannel<Unit>?,
        decryptionEngine: XmlDecryptionEngine,
        sharedIds: Set<String>
    ): Pair<List<IncomingTransaction>, List<Throwable>> {
        val results = transactions.mapAsync(progressChannel) {
            runCatching { decipherTransaction(it, decryptionEngine, sharedIds) }
        }

        val exceptions = results.mapNotNull { it.exceptionOrNull() }
        val decipheredTransactions = results.mapNotNull { it.getOrNull() }

        exceptions.forEach {
            if (it is SyncTransactionException) {
                transactionFailureRegistry.register(it.summary)
            }
        }

        return decipheredTransactions to exceptions
    }

    private fun decipherTransaction(
        downloadTransaction: SyncDownloadTransaction,
        decryptionEngine: XmlDecryptionEngine,
        sharedIds: Set<String>
    ): IncomingTransaction? {
        val (identifier, action, _, backupDate, type, content) = downloadTransaction

        val syncObjectType = SyncObjectType.forTransactionTypeOrNull(type) ?: return null

        val date = backupDate.toInstant()
        val transaction = Transaction(
            identifier,
            date
        )

        val isShared = sharedIds.contains(identifier)

        val incomingTransaction = when (action) {
            SyncDownloadTransaction.Action.BACKUP_EDIT -> {
                val transactionContent = content ?: throw SyncTransactionException(
                    transaction,
                    syncObjectType,
                    "Content shouldn't be null for an update transaction. "
                )
                val syncObject = decipherTransactionContent(
                    transactionContent,
                    decryptionEngine,
                    syncObjectType,
                    transaction
                )
                IncomingTransaction.Update(
                    transaction,
                    syncObject,
                    isShared
                )
            }
            SyncDownloadTransaction.Action.BACKUP_REMOVE ->
                IncomingTransaction.Delete(
                    transaction,
                    syncObjectType
                )
        }

        syncLogs.onDecipherTransaction(action.name, type, identifier, date)

        return incomingTransaction
    }

    private fun decipherTransactionContent(
        content: String,
        decryptionEngine: XmlDecryptionEngine,
        syncObjectType: SyncObjectType,
        transaction: Transaction
    ): SyncObject {
        val syncTransactionXml = try {
            decryptionEngine.decryptBase64ToXmlTransaction(content.asEncryptedBase64(), compressed = true)
        } catch (e: CryptographyException) {
            syncLogs.onDecipherTransactionError(syncObjectType.transactionType, transaction, e)
            
            val cipherPayload = if (e is CryptographyBase64Exception) null else ((e as? CryptographyEngineException)?.marker?.value ?: "")
            throw SyncTransactionException(
                transaction,
                syncObjectType,
                "Transaction deciphering failed. ",
                SyncCryptographyException(cause = e, cipherPayload = cipherPayload)
            )
        } catch (e: XmlException) {
            syncLogs.onDecipherTransactionError(syncObjectType.transactionType, transaction, e)
            throw SyncTransactionException(
                transaction,
                syncObjectType,
                "Transaction XML parsing failed. ",
                e
            )
        }

        return try {
            syncTransactionXml.toObject(syncObjectType)
        } catch (e: XmlTypeException) {
            syncLogs.onDecipherTransactionError(syncObjectType.transactionType, transaction, e)
            throw SyncTransactionException(
                transaction,
                syncObjectType,
                "XML contents invalid. ",
                e
            )
        }
    }

    override suspend fun cipherOutgoingTransactions(
        transactions: List<OutgoingTransaction>,
        cryptographyEngineFactory: CryptographyEngineFactory
    ): List<SyncUploadTransaction> = withContext(Dispatchers.Default) {
        cryptographyEngineFactory.createEncryptionEngine().forXml(xmlSerialization).use { encryptionEngine ->
            cipherOutgoingTransactions(transactions, encryptionEngine)
        }
    }

    private suspend fun cipherOutgoingTransactions(
        transactions: List<OutgoingTransaction>,
        encryptionEngine: XmlEncryptionEngine
    ): List<SyncUploadTransaction> {
        syncLogs.onCipherTransactionsStart()
        val cipheredTransactions = coroutineScope {
            transactions.map {
                async {
                    createOutgoingTransaction(encryptionEngine, it)
                }
            }.awaitAll()
        }
        syncLogs.onCipherTransactionsDone()
        return cipheredTransactions
    }

    private fun createOutgoingTransaction(
        encryptionEngine: XmlEncryptionEngine,
        outgoingTransaction: OutgoingTransaction
    ): SyncUploadTransaction {
        val (action, cipheredContent) = when (outgoingTransaction) {
            is OutgoingTransaction.Update -> {
                val syncObject = outgoingTransaction.syncObject.copy {
                    
                    id = outgoingTransaction.identifier
                }
                val transaction = syncObject.toTransaction()
                val encryptedData = encryptionEngine.encryptXmlTransactionToBase64String(transaction)
                SyncUploadTransaction.Action.BACKUP_EDIT to encryptedData
            }
            is OutgoingTransaction.Delete ->
                SyncUploadTransaction.Action.BACKUP_REMOVE to null
        }

        syncLogs.onCipherTransaction(
            action.name,
            outgoingTransaction.syncObjectType.transactionType,
            outgoingTransaction.identifier,
            outgoingTransaction.date
        )

        return SyncUploadTransaction(
            type = outgoingTransaction.syncObjectType.transactionType,
            action = action,
            time = outgoingTransaction.date.toInstantEpochSecond(),
            identifier = outgoingTransaction.identifier,
            content = cipheredContent?.value
        )
    }
}
