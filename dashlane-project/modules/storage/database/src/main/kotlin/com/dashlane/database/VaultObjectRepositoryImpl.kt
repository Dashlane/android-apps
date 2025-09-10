package com.dashlane.database

import com.dashlane.database.VaultObjectRepositoryImpl.Companion.DEFAULT_CHUNK_PART
import com.dashlane.database.adapter.CountryAdapter
import com.dashlane.database.adapter.InstantAdapter
import com.dashlane.database.adapter.LocalDateAdapter
import com.dashlane.database.adapter.SyncObjectAdapter
import com.dashlane.database.adapter.SyncStateAdapter
import com.dashlane.database.transaction.CommonTransaction
import com.dashlane.database.transaction.RecoverableRepository
import com.dashlane.database.transaction.Statistics
import com.dashlane.database.transaction.Timings
import com.dashlane.database.transaction.TransactionResult
import com.dashlane.database.transaction.performPendingTransaction
import com.dashlane.database.transaction.performTransaction
import com.dashlane.sync.VaultItemBackupWrapper
import com.dashlane.util.updateCollection
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.DatabaseDataChangeHistorySummary
import com.dashlane.vault.summary.DatabaseSummary
import com.dashlane.vault.summary.DatabaseSyncSummary
import com.dashlane.vault.summary.toDatabaseDataChangeHistorySummary
import com.dashlane.vault.summary.toDatabaseSyncSummary
import com.dashlane.vault.summary.toSummaryOrNull
import com.dashlane.vault.summary.toSyncSummary
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.XmlTypeException
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.isSupportedSyncObjectType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.FileNotFoundException
import okio.buffer
import java.io.File
import java.time.Instant

@OptIn(ExperimentalStdlibApi::class)
internal class VaultObjectRepositoryImpl(
    val files: DatabaseFiles,
    private val reader: DatabaseReader,
    private val writer: DatabaseWriter,
    private val memorySummaryRepository: MemorySummaryRepositoryMutable,
    private val summaryRepository: FileSummaryRepository,
    private val syncSummaryRepository: SyncSummaryRepository,
    private val transactionRepository: TransactionRepository,
    private val backupRepository: BackupRepository,
    private val dataChangeHistoryRepository: DataChangeHistoryRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : VaultObjectRepository, RecoverableRepository {
    private val mutex = Mutex()

    private val adapter: JsonAdapter<VaultItem<SyncObject>> =
        Moshi.Builder()
            .add(InstantAdapter)
            .add(LocalDateAdapter)
            .add(CountryAdapter)
            .add(SyncStateAdapter)
            .add(SyncObjectAdapter)
            .build()
            .adapter<VaultItem<SyncObject>>()
            .nullSafe()

    override suspend fun get(id: Id): VaultItem<*>? = withContext(ioDispatcher) {
        val file = files.content[id]
        return@withContext try {
            readVaultItem(file)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    override suspend fun transaction(
        timeoutMillis: Long,
        block: suspend VaultObjectRepository.Transaction.() -> Unit,
    ): TransactionResult? {
        return withContext(ioDispatcher) {
            val transaction = TransactionImpl()
            transaction.performTransaction(mutex, timeoutMillis) {
                block(transaction)
            }
        }
    }

    suspend fun rebuildSummary(): TransactionResult? {
        val transaction = TransactionImpl()
        return try {
            withTimeout(VaultObjectRepository.DEFAULT_TIMEOUT_MILLIS) {
                mutex.lock(transaction)
            }
            transaction.rebuildSummary()
        } catch (e: Exception) {
            throw DatabaseException(message = "rebuildSummary fails", cause = e)
        } finally {
            try {
                mutex.unlock(transaction)
            } catch (e: IllegalStateException) {
                
            }
        }
    }

    override suspend fun applyPendingTransaction() {
        val transaction = TransactionImpl()
        transaction.performPendingTransaction(mutex)
    }

    private fun readVaultItem(file: File): VaultItem<*>? {
        return runCatching {
            reader.read(file) { source ->
                adapter.fromJson(JsonReader.of(source.buffer()))
            }
        }.getOrElse { exception ->
            
            if (exception.cause is XmlTypeException) {
                null
            } else {
                throw exception
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class TransactionImpl : VaultObjectRepository.Transaction, CommonTransaction {
        private var updates = mutableMapOf<Id, VaultItem<*>>()

        private var deletes = mutableSetOf<Id>()
        private var backups = mutableMapOf<Id, XmlTransaction>()

        private val builder = Timings.Builder()

        override fun update(vaultObject: VaultItem<*>) {
            if (!vaultObject.syncObject.isSupportedSyncObjectType) return
            val id = Id.of(vaultObject.uid)
            deletes.remove(id)
            updates[id] = vaultObject
        }

        override fun delete(id: Id) {
            updates.remove(id)
            backups.remove(id)
            deletes.add(id)
        }

        override fun update(data: VaultItemBackupWrapper<*>) {
            val vaultItem = data.vaultItem
            if (!vaultItem.syncObject.isSupportedSyncObjectType) return
            update(vaultItem)
            data.backup?.also { backups[Id.of(vaultItem.uid)] = it }
        }

        override suspend fun closeTransaction(): TransactionResult? {
            builder.transactionStart = Instant.now()
            if (updates.isEmpty() && backups.isEmpty() && deletes.isEmpty()) {
                return null
            }
            val allItems = updates.values
            val dataChangeHistories = allItems
                .filter { it.syncObjectType == SyncObjectType.DATA_CHANGE_HISTORY } as List<VaultItem<SyncObject.DataChangeHistory>>

            
            val vaultItems = allItems - dataChangeHistories

            
            writeTransaction(allItems, deletes, backups)

            val summaryResult = writeSummaries(
                databaseSummary = memorySummaryRepository.databaseSummary,
                databaseSyncSummary = memorySummaryRepository.databaseSyncSummary,
                databaseDataChangeHistorySummary = memorySummaryRepository.databaseDataChangeHistorySummary,
                vaultItems = vaultItems,
                allItems = allItems,
                dataChangeHistories = dataChangeHistories,
                itemsToDelete = deletes,
            )

            
            replaceMemoryDatabaseSummary(summaryResult.databaseSummary)
            replaceMemoryDatabaseSyncSummary(summaryResult.databaseSyncSummary)
            replaceMemoryDatabaseDataChangeHistorySummary(summaryResult.databaseDataChangeHistorySummary)
            transactionRepository.withMarker {
                applyTransaction()
            }
            builder.transactionEnd = Instant.now()

            return TransactionResult(
                statistics = Statistics(
                    summaryCount = memorySummaryRepository.databaseSummary?.data?.size,
                    syncSummaryCount = memorySummaryRepository.databaseSyncSummary?.items?.size,
                    dataChangeHistorySummaryCount = memorySummaryRepository.databaseDataChangeHistorySummary?.data?.size,
                    updateCount = updates.size,
                    deleteCount = deletes.size,
                ),
                timings = builder.build(),
            )
        }

        override suspend fun applyTransaction() {
            builder.applyTransactionStart = Instant.now()
            applyItemsDirectory()
            applyBackupDirectory()
            applySummaryFiles()
            applyDeletion()
            builder.applyTransactionEnd = Instant.now()
        }

        private fun applyDeletion() {
            files.transactionDelete.deleteDirectory.listFiles()?.forEach { file ->
                files.content.apply {
                    getItemFile(file.name).delete()
                    getBackupFile(file.name).delete()
                }
            }
        }

        private fun applySummaryFiles() {
            files.content.summaryFile.apply {
                delete()
                files.transactionContent.summaryFile.renameTo(this)
            }
            files.content.syncSummaryFile.apply {
                delete()
                files.transactionContent.syncSummaryFile.renameTo(this)
            }
            files.content.dataChangeHistorySummaryFile.apply {
                delete()
                files.transactionContent.dataChangeHistorySummaryFile.renameTo(this)
            }
        }

        private fun applyBackupDirectory() {
            if (files.content.backupDirectory.isDirectoryEmpty()) {
                files.transactionContent.backupDirectory.renameTo(files.content.backupDirectory)
            } else {
                files.transactionContent.backupDirectory.listFiles()?.forEach { transactionFile ->
                    files.content.getBackupFile(transactionFile.name).apply {
                        this.delete()
                        transactionFile.renameTo(this)
                    }
                }
            }
        }

        private fun applyItemsDirectory() {
            if (files.content.itemsDirectory.isDirectoryEmpty()) {
                files.transactionContent.itemsDirectory.renameTo(files.content.itemsDirectory)
            } else {
                files.transactionContent.itemsDirectory.listFiles()?.forEach { transactionFile ->
                    files.content.getItemFile(transactionFile.name).apply {
                        this.delete()
                        transactionFile.renameTo(this)
                    }
                }
            }
        }

        private suspend fun writeTransaction(
            vaultItems: Collection<VaultItem<*>>,
            itemsToDelete: Set<Id>,
            backups: Map<Id, XmlTransaction>,
        ) {
            coroutineScope {
                builder.writeItemsStart = Instant.now()
                vaultItems.chunkedAndMapAsync { list ->
                    list.forEach { vaultObject ->
                        val id = Id.of(vaultObject.uid)
                        val transactionFile = files.transactionContent[id]

                        writer.write(transactionFile) {
                            adapter.toJson(it, vaultObject)
                        }
                    }
                }
                builder.writeItemsEnd = Instant.now()

                builder.writeBackupsStart = Instant.now()
                backups.entries.chunkedAndMapAsync { list ->
                    list.forEach { (id, backup) ->
                        backupRepository.save(id, backup)
                    }
                }
                builder.writeBackupsEnd = Instant.now()

                builder.deleteStart = Instant.now()
                itemsToDelete.chunkedAndMapAsync { list ->
                    list.forEach { id ->
                        val transactionDeleteFile = files.transactionDelete.getFileToDelete(id)
                        transactionDeleteFile.createNewFile()
                    }
                }
                builder.deleteEnd = Instant.now()
            }
        }

        private fun saveDatabaseDataChangeHistorySummary(
            databaseDataChangeHistorySummary: DatabaseDataChangeHistorySummary,
        ) {
            dataChangeHistoryRepository.save(databaseDataChangeHistorySummary)
        }

        private suspend fun saveDatabaseSummary(databaseSummary: DatabaseSummary) {
            summaryRepository.save(databaseSummary)
        }

        private fun replaceMemoryDatabaseSummary(databaseSummary: DatabaseSummary) {
            memorySummaryRepository.replace(databaseSummary)
        }

        private fun replaceMemoryDatabaseSyncSummary(databaseSummary: DatabaseSyncSummary) {
            memorySummaryRepository.replace(databaseSummary)
        }

        private fun replaceMemoryDatabaseDataChangeHistorySummary(dataChangeHistorySummary: DatabaseDataChangeHistorySummary) {
            memorySummaryRepository.replace(dataChangeHistorySummary)
        }

        private suspend fun saveDatabaseSyncSummary(databaseSyncSummary: DatabaseSyncSummary) {
            syncSummaryRepository.save(databaseSyncSummary)
        }

        suspend fun rebuildSummary(): TransactionResult? {
            builder.transactionStart = Instant.now()
            if (files.content.itemsDirectory.isDirectoryEmpty()) return null

            val allItems: List<VaultItem<*>> =
                files.content.itemsDirectory.walk().filter { it.isFile }
                    .chunkedAndMapAsync { list ->
                        list.mapNotNull { readVaultItem(it) }
                    }.flatten()

            if (allItems.isEmpty()) return null

            val dataChangeHistories = allItems
                .filter { it.syncObjectType == SyncObjectType.DATA_CHANGE_HISTORY } as List<VaultItem<SyncObject.DataChangeHistory>>

            
            val vaultItems = allItems - dataChangeHistories
            val summaryResult = writeSummaries(
                vaultItems = vaultItems,
                allItems = allItems,
                dataChangeHistories = dataChangeHistories,
                itemsToDelete = emptySet(),
            )
            
            replaceMemoryDatabaseSummary(summaryResult.databaseSummary)
            replaceMemoryDatabaseSyncSummary(summaryResult.databaseSyncSummary)
            replaceMemoryDatabaseDataChangeHistorySummary(summaryResult.databaseDataChangeHistorySummary)
            transactionRepository.withMarker {
                applyTransaction()
            }
            builder.transactionEnd = Instant.now()
            return TransactionResult(
                statistics = Statistics(
                    summaryCount = memorySummaryRepository.databaseSummary?.data?.size,
                    syncSummaryCount = memorySummaryRepository.databaseSyncSummary?.items?.size,
                    dataChangeHistorySummaryCount = memorySummaryRepository.databaseDataChangeHistorySummary?.data?.size,
                    updateCount = updates.size,
                    deleteCount = deletes.size,
                ),
                timings = builder.build(),
            )
        }

        private suspend fun writeSummaries(
            databaseSummary: DatabaseSummary? = null,
            databaseSyncSummary: DatabaseSyncSummary? = null,
            databaseDataChangeHistorySummary: DatabaseDataChangeHistorySummary? = null,
            vaultItems: List<VaultItem<*>>,
            allItems: Collection<VaultItem<*>>,
            dataChangeHistories: List<VaultItem<SyncObject.DataChangeHistory>>,
            itemsToDelete: Set<Id>,
        ): SummaryResult {
            builder.summaryStart = Instant.now()
            return coroutineScope {
                val deferredSummary = async {
                    
                    val list = databaseSummary?.data ?: emptyList()
                    list.updateCollection(
                        vaultItems.mapNotNull { it.toSummaryOrNull() },
                        itemsToDelete,
                    ) {
                        Id.of(it.id)
                    }.let { DatabaseSummary(it) }.apply {
                        saveDatabaseSummary(this)
                    }
                }

                val deferredSyncSummary = async {
                    
                    val list = databaseSyncSummary?.items ?: emptyList()
                    list.updateCollection(
                        allItems.map { it.toSyncSummary() },
                        itemsToDelete,
                    ) {
                        Id.of(it.id)
                    }.toDatabaseSyncSummary().apply {
                        saveDatabaseSyncSummary(this)
                    }
                }

                val deferredDataChangeHistorySummary = async {
                    
                    val list = databaseDataChangeHistorySummary?.data ?: emptyList()
                    list.updateCollection(
                        dataChangeHistories.mapNotNull { it.toSummaryOrNull() },
                        itemsToDelete,
                    ) {
                        Id.of(it.id)
                    }.toDatabaseDataChangeHistorySummary().apply {
                        saveDatabaseDataChangeHistorySummary(this)
                    }
                }
                SummaryResult(
                    databaseSummary = deferredSummary.await(),
                    databaseSyncSummary = deferredSyncSummary.await(),
                    databaseDataChangeHistorySummary = deferredDataChangeHistorySummary.await(),
                ).also {
                    builder.summaryEnd = Instant.now()
                }
            }
        }
    }

    companion object {
        internal const val DEFAULT_CHUNK_PART = 4
    }
}

internal suspend inline fun <R, T> Sequence<T>.chunkedAndMapAsync(
    crossinline transform: suspend (List<T>) -> R,
): List<R> = coroutineScope {
    chunked(maxOf(this@chunkedAndMapAsync.count() / DEFAULT_CHUNK_PART, 1))
        .map { list -> async { transform(list) } }
        .toList()
        .awaitAll()
}

internal suspend inline fun <R, T> Iterable<T>.chunkedAndMapAsync(
    crossinline transform: suspend (List<T>) -> R,
): List<R> = coroutineScope {
    chunked(maxOf(this@chunkedAndMapAsync.count() / DEFAULT_CHUNK_PART, 1))
        .map { list -> async { transform(list) } }
        .awaitAll()
}

private data class SummaryResult(
    val databaseSummary: DatabaseSummary,
    val databaseSyncSummary: DatabaseSyncSummary,
    val databaseDataChangeHistorySummary: DatabaseDataChangeHistorySummary,
)