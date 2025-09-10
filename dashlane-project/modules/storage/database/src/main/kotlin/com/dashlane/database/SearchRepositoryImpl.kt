package com.dashlane.database

import com.dashlane.database.adapter.InstantAdapter
import com.dashlane.database.model.DatabaseSearch
import com.dashlane.database.model.SearchItem
import com.dashlane.database.model.toDatabaseSearch
import com.dashlane.database.transaction.CommonTransaction
import com.dashlane.database.transaction.RecoverableRepository
import com.dashlane.database.transaction.TransactionResult
import com.dashlane.database.transaction.performPendingTransaction
import com.dashlane.database.transaction.performTransaction
import com.dashlane.util.updateCollection
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import okio.buffer

internal class SearchRepositoryImpl(
    private val files: DatabaseFiles,
    private val reader: DatabaseReader,
    private val writer: DatabaseWriter,
    private val transactionRepository: TransactionRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : SearchRepository, RecoverableRepository {
    private val mutex = Mutex()

    private val jsonAdapter: JsonAdapter<DatabaseSearch> =
        Moshi.Builder()
            .add(InstantAdapter)
            .build()
            .adapter(DatabaseSearch::class.java)
            .nullSafe()

    override suspend fun load(): DatabaseSearch {
        return withContext(ioDispatcher) {
            val file = files.content.searchQueryFile
            return@withContext runCatching {
                reader.read(file) { source ->
                    jsonAdapter.fromJson(source.buffer())
                } ?: DatabaseSearch(emptyList())
            }.getOrElse { DatabaseSearch(emptyList()) }
        }
    }

    override suspend fun transaction(
        timeoutMillis: Long,
        block: suspend SearchRepository.Transaction.() -> Unit,
    ) {
        withContext(ioDispatcher) {
            val transaction = TransactionImpl()
            transaction.performTransaction(mutex, timeoutMillis) {
                block(transaction)
            }
        }
    }

    override suspend fun applyPendingTransaction() {
        val transaction = TransactionImpl()
        transaction.performPendingTransaction(mutex)
    }

    private inner class TransactionImpl : SearchRepository.Transaction, CommonTransaction {
        private val updates = mutableMapOf<String, SearchItem>()
        private val deletes = mutableSetOf<String>()

        override fun update(item: SearchItem) {
            val id = item.dataId
            deletes.remove(id)
            updates[id] = item
        }

        override fun delete(ids: Set<String>) {
            ids.forEach {
                updates.remove(it)
            }
            deletes.addAll(ids)
        }

        override suspend fun closeTransaction(): TransactionResult? {
            saveSearchItems()
            transactionRepository.withMarker {
                applyTransaction()
            }
            return null
        }

        override suspend fun applyTransaction() {
            files.content.searchQueryFile.apply {
                delete()
                files.transactionSearchContent.searchQueryFile.renameTo(this)
            }
        }

        private suspend fun saveSearchItems() {
            val toSave = load().items.updateCollection(updates.values, deletes) {
                it.dataId
            }.toDatabaseSearch()
            val file = files.transactionSearchContent.searchQueryFile
            writer.write(file) { sink ->
                jsonAdapter.toJson(sink, toSave)
            }
        }
    }
}