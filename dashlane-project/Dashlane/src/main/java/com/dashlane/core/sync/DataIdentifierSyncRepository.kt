package com.dashlane.core.sync

import android.content.ContentValues
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.storage.userdata.DataSyncDao
import com.dashlane.storage.userdata.DatabaseItemSaverImplLegacy
import com.dashlane.storage.userdata.PendingSyncItem
import com.dashlane.sync.DataIdentifierExtraDataWrapper
import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.domain.Transaction
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.sync.xml.MergeListStrategy
import com.dashlane.sync.xml.mergeInto
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getTableName
import com.dashlane.vault.util.syncObjectType
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.objectType
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import com.dashlane.xml.serializer.XmlSerialization
import java.time.Instant
import javax.inject.Inject
import kotlin.reflect.KClass



class DataIdentifierSyncRepository(
    private val dao: DataSyncDao,
    private val databaseItemSaver: DatabaseItemSaverImplLegacy,
    private val transactionMarshaller: XmlSerialization
) {
    @Inject
    constructor(
        dao: DataSyncDao,
        databaseItemSaver: DatabaseItemSaverImplLegacy
    ) : this(
        dao,
        databaseItemSaver,
        XmlSerialization
    )

    fun insertOrUpdateForSync(vaultItem: VaultItem<*>, backup: XmlTransaction, backupTimeMillis: Long) {
        val itemXML = transactionMarshaller.serializeTransaction(backup)
        val backupDate = Instant.ofEpochMilli(backupTimeMillis)
        val dataWrapper = DataIdentifierExtraDataWrapper(vaultItem, itemXML, backupDate)
        databaseItemSaver.saveItemFromPersonalSync(dataWrapper)
    }

    fun deleteForSync(kClass: KClass<out SyncObject>, uuid: String) {
        databaseItemSaver.delete(uuid, kClass.syncObjectType)
    }

    fun getOutgoingTransactions(kClass: KClass<out SyncObject>): List<OutgoingTransaction> {
        val dataType = kClass.syncObjectType
        val pendingItems = dao.getItemsPendingSyncForType(dataType)
        return pendingItems.map { createOutgoingTransaction(it) }
    }

    fun fetchAsOutgoingUpdate(
        kClass: KClass<out SyncObject>,
        uuids: List<String>
    ): List<OutgoingTransaction.Update> {
        val dataType = kClass.syncObjectType
        return dao.getItemsWithExtraData(uuids, dataType).map {
            createOutgoingTransaction(it, SyncState.IN_SYNC_MODIFIED) as OutgoingTransaction.Update
        }
    }

    private fun createOutgoingTransaction(
        it: PendingSyncItem,
        forcedSyncState: SyncState? = null
    ): OutgoingTransaction {
        val dataIdentifierExtraDataWrapper = it.first
        val vaultItem = dataIdentifierExtraDataWrapper.vaultItem
        val transaction = Transaction(
            vaultItem.uid,
            dataIdentifierExtraDataWrapper.backupDate ?: Instant.EPOCH
        )
        val syncObjectType = vaultItem.syncObject.objectType
        return when (forcedSyncState ?: it.second) {
            SyncState.IN_SYNC_MODIFIED, SyncState.MODIFIED -> {
                val backupXml = dataIdentifierExtraDataWrapper.extraData
                    ?.let { tryOrNull { transactionMarshaller.deserializeTransaction(it) } }
                val mergedObjectNode = vaultItem.syncObject
                    .toTransaction().node
                    .mergeInto(backupXml?.node, MergeListStrategy.KEEP_RICHEST)
                val syncObject = XmlTransaction(mergedObjectNode).toObject(syncObjectType)
                OutgoingTransaction.Update(
                    transaction,
                    syncObject,
                    backupXml?.toObject(syncObjectType) ?: syncObject,
                    vaultItem.isShared()
                )
            }
            SyncState.IN_SYNC_DELETED, SyncState.DELETED -> {
                OutgoingTransaction.Delete(
                    transaction,
                    syncObjectType
                )
            }
            else -> error("Unexpected state ${it.second} for ${dataIdentifierExtraDataWrapper.vaultItem}")
        }
    }

    fun clearPendingOperations() {
        dao.markAllItemsAsSynced()
    }

    fun getSummary(syncObjectTypes: List<SyncObjectType>): Collection<SyncSummaryItem> {
        return syncObjectTypes.flatMap {
            dao.getItemsForSummary(it).map { (id, lastBackupDate) ->
                SyncSummaryItem(id, lastBackupDate, it)
            }
        }
    }

    fun applyBackupDate(kClass: KClass<out SyncObject>, uuid: String, backupTime: Instant) {
        applyBackupDate(kClass.syncObjectType, backupTime, uuid)
    }

    private fun applyBackupDate(dataType: SyncObjectType, backupTime: Instant, uid: String) {
        val tableName = checkNotNull(dataType.getTableName()) { "No table associated with $dataType" }
        val contentValues = ContentValues().apply {
            put(DataIdentifierSql.FIELD_BACKUP_DATE, backupTime.epochSecond)
        }
        val whereStatement = DataIdentifierSql.FIELD_UID + " = ?"
        val whereArgs = arrayOf(uid)
        databaseItemSaver.updateItems(contentValues, tableName, whereStatement, whereArgs)
    }

    fun preparePendingOperations(kClasses: List<KClass<out SyncObject>>) {
        val dataTypes = kClasses.map { it.syncObjectType }
        dao.markItemsInSync(dataTypes)
    }

    fun fetchDuplicate(
        type: SyncObjectType
    ): List<List<OutgoingTransaction.Update>> =
        dao.getDuplicationCandidates(type).map { items ->
            items.map { item ->
                createOutgoingTransaction(item, SyncState.IN_SYNC_MODIFIED) as OutgoingTransaction.Update
            }
        }

    fun flagForDeletion(it: OutgoingTransaction.Update) {
        dao.markItemSyncState(it.syncObjectType, it.identifier, SyncState.DELETED)
    }
}
