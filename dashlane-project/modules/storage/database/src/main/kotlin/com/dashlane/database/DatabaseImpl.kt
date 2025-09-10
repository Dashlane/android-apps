package com.dashlane.database

import com.dashlane.database.DatabaseIndex.Companion.SUMMARY_VERSION
import com.dashlane.database.transaction.TransactionRecovery
import kotlinx.coroutines.CoroutineDispatcher

internal class DatabaseImpl(
    private val files: DatabaseFiles,
    reader: DatabaseReader,
    writer: DatabaseWriter,
    private val cryptographyEngineFactory: CryptographyEngineFactoryImpl,
    private val indexReader: IndexReader,
    private val indexWriter: IndexWriter,
    private val ioDispatcher: CoroutineDispatcher,
) : Database {
    private val transactionRepository = TransactionRepositoryImpl(files)
    private val transactionSharingRepository = TransactionSharingRepositoryImpl(files)
    private val transactionSearchRepository = TransactionSearchRepositoryImpl(files)

    private val fileSummaryRepository: FileSummaryRepository =
        FileSummaryRepositoryImpl(files, reader, writer)

    private val dataChangeHistoryRepository: DataChangeHistoryRepository =
        DataChangeHistoryRepositoryImpl(files, reader, writer)

    private val syncSummaryRepository: SyncSummaryRepository =
        SyncSummaryRepositoryImpl(files, reader, writer)

    override val backupRepository: BackupRepository =
        BackupRepositoryImpl(files, reader, writer, ioDispatcher)

    override val memorySummaryRepository: MemorySummaryRepositoryMutable =
        MemorySummaryRepositoryImpl(
            fileSummaryRepository = fileSummaryRepository,
            syncSummaryRepository = syncSummaryRepository,
            dataChangeHistoryRepository = dataChangeHistoryRepository,
        )

    override val searchRepository: SearchRepositoryImpl =
        SearchRepositoryImpl(files, reader, writer, transactionSearchRepository, ioDispatcher)

    override val vaultObjectRepository = VaultObjectRepositoryImpl(
        files = files,
        reader = reader,
        writer = writer,
        memorySummaryRepository = memorySummaryRepository,
        syncSummaryRepository = syncSummaryRepository,
        summaryRepository = fileSummaryRepository,
        transactionRepository = transactionRepository,
        backupRepository = backupRepository,
        dataChangeHistoryRepository = dataChangeHistoryRepository,
        ioDispatcher = ioDispatcher,
    )

    override val sharingRepository: SharingRepositoryImpl =
        SharingRepositoryImpl(files, reader, writer, transactionSharingRepository, ioDispatcher)

    internal suspend fun rebuildSummaryIfNeeded() {
        val databaseIndex = readIndex() ?: return
        if (databaseIndex.summaryVersion >= SUMMARY_VERSION) return
        vaultObjectRepository.rebuildSummary()
        indexWriter.save(
            databaseIndex.copy(summaryVersion = SUMMARY_VERSION),
        )
    }

    internal fun readIndex() = indexReader.read(cryptographyEngineFactory.key)

    internal suspend fun recovery() {
        TransactionRecovery(
            transactionRepository,
            transactionSharingRepository,
            transactionSearchRepository,
            vaultObjectRepository,
            sharingRepository,
            searchRepository,
        ).recovery()
    }

    internal fun delete() {
        files.delete()
        close()
    }

    internal fun close() {
        cryptographyEngineFactory.close()
    }
}