package com.dashlane.database

import com.dashlane.database.adapter.InstantAdapter
import com.dashlane.database.adapter.LocalDateAdapter
import com.dashlane.database.transaction.CommonTransaction
import com.dashlane.database.transaction.RecoverableRepository
import com.dashlane.database.transaction.TransactionResult
import com.dashlane.database.transaction.performPendingTransaction
import com.dashlane.database.transaction.performTransaction
import com.dashlane.sharing.model.CollectionRaclette
import com.dashlane.sharing.model.ItemContentRaclette
import com.dashlane.sharing.model.ItemGroupRaclette
import com.dashlane.sharing.model.SharingCollections
import com.dashlane.sharing.model.SharingItemContents
import com.dashlane.sharing.model.SharingItemGroups
import com.dashlane.sharing.model.SharingUserGroups
import com.dashlane.sharing.model.UserGroupRaclette
import com.dashlane.sharing.model.toSharingCollections
import com.dashlane.sharing.model.toSharingItemContents
import com.dashlane.sharing.model.toSharingItemGroups
import com.dashlane.sharing.model.toSharingUserGroups
import com.dashlane.util.updateCollection
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import okio.buffer

internal class SharingRepositoryImpl(
    private val files: DatabaseFiles,
    private val reader: DatabaseReader,
    private val writer: DatabaseWriter,
    private val transactionRepository: TransactionRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : SharingRepository, RecoverableRepository {
    private val mutex = Mutex()

    private val jsonAdapterItemGroups: JsonAdapter<SharingItemGroups> =
        Moshi.Builder()
            .add(InstantAdapter)
            .add(LocalDateAdapter)
            .build()
            .adapter(SharingItemGroups::class.java)
            .nullSafe()

    private val jsonAdapterItemContents: JsonAdapter<SharingItemContents> =
        Moshi.Builder()
            .add(InstantAdapter)
            .add(LocalDateAdapter)
            .build()
            .adapter(SharingItemContents::class.java)
            .nullSafe()

    private val jsonAdapterUserGroups: JsonAdapter<SharingUserGroups> =
        Moshi.Builder()
            .add(InstantAdapter)
            .add(LocalDateAdapter)
            .build()
            .adapter(SharingUserGroups::class.java)
            .nullSafe()

    private val jsonAdapterCollections: JsonAdapter<SharingCollections> =
        Moshi.Builder()
            .add(InstantAdapter)
            .add(LocalDateAdapter)
            .build()
            .adapter(SharingCollections::class.java)
            .nullSafe()

    override suspend fun transaction(
        timeoutMillis: Long,
        block: suspend SharingRepository.Transaction.() -> Unit,
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

    override suspend fun loadItemGroups(): List<ItemGroupRaclette> = withContext(ioDispatcher) {
        loadSharingItemGroups().itemGroups
    }

    override suspend fun loadUserGroups(): List<UserGroupRaclette> = withContext(ioDispatcher) {
        loadSharingUserGroups().userGroups
    }

    override suspend fun loadItemContents(): List<ItemContentRaclette> = withContext(ioDispatcher) {
        loadSharingItemContents().itemContents
    }

    override suspend fun loadCollections(): List<CollectionRaclette> = withContext(ioDispatcher) {
        loadSharingCollections().collections
    }

    private fun loadSharingItemGroups(): SharingItemGroups {
        val file = files.content.sharingItemGroupsFile
        return reader.read(file) { source ->
            jsonAdapterItemGroups.fromJson(source.buffer())
        } ?: SharingItemGroups(emptyList())
    }

    private fun loadSharingUserGroups(): SharingUserGroups {
        val file = files.content.sharingUserGroupsFile
        return reader.read(file) { source ->
            jsonAdapterUserGroups.fromJson(source.buffer())
        } ?: SharingUserGroups(emptyList())
    }

    private fun loadSharingItemContents(): SharingItemContents {
        val file = files.content.sharingItemContentsFile
        return reader.read(file) { source ->
            jsonAdapterItemContents.fromJson(source.buffer())
        } ?: SharingItemContents(emptyList())
    }

    private fun loadSharingCollections(): SharingCollections {
        val file = files.content.sharingCollectionsFile
        return reader.read(file) { source ->
            jsonAdapterCollections.fromJson(source.buffer())
        } ?: SharingCollections(emptyList())
    }

    private inner class TransactionImpl : SharingRepository.Transaction, CommonTransaction {
        private val updateItemGroups = mutableMapOf<String, ItemGroupRaclette>()
        private val updateUserGroups = mutableMapOf<String, UserGroupRaclette>()
        private val updateItemContents = mutableMapOf<String, ItemContentRaclette>()
        private val updateCollections = mutableMapOf<String, CollectionRaclette>()

        private val deleteItemGroups = mutableSetOf<String>()
        private val deleteUserGroups = mutableSetOf<String>()
        private val deleteItemContents = mutableSetOf<String>()
        private val deleteCollections = mutableSetOf<String>()

        override fun deleteItemGroups(ids: Set<String>) {
            if (ids.isEmpty()) return
            deleteItemGroups.addAll(ids)
            ids.forEach {
                updateItemGroups.remove(it)
            }
        }

        override fun deleteUserGroups(ids: Set<String>) {
            if (ids.isEmpty()) return
            deleteUserGroups.addAll(ids)
            ids.forEach {
                updateUserGroups.remove(it)
            }
        }

        override fun deleteItemContents(ids: Set<String>) {
            if (ids.isEmpty()) return
            deleteItemContents.addAll(ids)
            ids.forEach {
                updateItemContents.remove(it)
            }
        }

        override fun deleteCollections(ids: Set<String>) {
            if (ids.isEmpty()) return
            deleteCollections.addAll(ids)
            ids.forEach {
                updateCollections.remove(it)
            }
        }

        override fun updateItemGroups(itemGroups: List<ItemGroupRaclette>) {
            if (itemGroups.isEmpty()) return
            itemGroups.forEach {
                deleteItemGroups.remove(it.groupId)
                updateItemGroups[it.groupId] = it
            }
        }

        override fun updateItemContents(itemContents: List<ItemContentRaclette>) {
            if (itemContents.isEmpty()) return
            itemContents.forEach {
                deleteItemContents.remove(it.itemId)
                updateItemContents[it.itemId] = it
            }
        }

        override fun updateUserGroups(userGroups: List<UserGroupRaclette>) {
            if (userGroups.isEmpty()) return
            userGroups.forEach {
                deleteUserGroups.remove(it.groupId)
                updateUserGroups[it.groupId] = it
            }
        }

        override fun updateCollections(collections: List<CollectionRaclette>) {
            if (collections.isEmpty()) return
            collections.forEach {
                deleteCollections.remove(it.uuid)
                updateCollections[it.uuid] = it
            }
        }

        override suspend fun closeTransaction(): TransactionResult? {
            saveItemGroups()
            saveUserGroups()
            saveItemContents()
            saveCollections()
            transactionRepository.withMarker {
                applyTransaction()
            }
            return null
        }

        override suspend fun applyTransaction() {
            files.content.sharingItemGroupsFile.apply {
                delete()
                files.transactionSharingContent.sharingItemGroupsFile.renameTo(this)
            }
            files.content.sharingUserGroupsFile.apply {
                delete()
                files.transactionSharingContent.sharingUserGroupsFile.renameTo(this)
            }
            files.content.sharingItemContentsFile.apply {
                delete()
                files.transactionSharingContent.sharingItemContentsFile.renameTo(this)
            }
            files.content.sharingCollectionsFile.apply {
                delete()
                files.transactionSharingContent.sharingCollectionsFile.renameTo(this)
            }
        }

        private fun saveItemGroups() {
            val toSave = loadSharingItemGroups().itemGroups
                .updateCollection(updateItemGroups.values, deleteItemGroups) { it.groupId }
                .toSharingItemGroups()
            val file = files.transactionSharingContent.sharingItemGroupsFile
            writer.write(file) { sink ->
                jsonAdapterItemGroups.toJson(sink, toSave)
            }
        }

        private fun saveUserGroups() {
            val toSave = loadSharingUserGroups().userGroups
                .updateCollection(updateUserGroups.values, deleteUserGroups) { it.groupId }
                .toSharingUserGroups()
            val file = files.transactionSharingContent.sharingUserGroupsFile
            writer.write(file) { sink ->
                jsonAdapterUserGroups.toJson(sink, toSave)
            }
        }

        private fun saveItemContents() {
            val toSave = loadSharingItemContents().itemContents
                .updateCollection(updateItemContents.values, deleteItemContents) { it.itemId }
                .toSharingItemContents()
            val file = files.transactionSharingContent.sharingItemContentsFile
            writer.write(file) { sink ->
                jsonAdapterItemContents.toJson(sink, toSave)
            }
        }

        private fun saveCollections() {
            val toSave = loadSharingCollections().collections
                .updateCollection(updateCollections.values, deleteCollections) { it.uuid }
                .toSharingCollections()
            val file = files.transactionSharingContent.sharingCollectionsFile
            writer.write(file) { sink ->
                jsonAdapterCollections.toJson(sink, toSave)
            }
        }
    }
}