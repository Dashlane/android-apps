package com.dashlane.sync

import com.dashlane.database.VaultObjectRepository
import com.dashlane.storage.userdata.DataSyncDaoRaclette
import com.dashlane.storage.userdata.DatabaseItemSaverRaclette
import com.dashlane.storage.userdata.PendingSyncItemRaclette
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.domain.Transaction
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.sync.xml.MergeListStrategy
import com.dashlane.sync.xml.mergeInto
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.objectType
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import java.time.Instant
import javax.inject.Inject

class DataIdentifierSyncRepositoryRaclette @Inject constructor(
    private val dao: DataSyncDaoRaclette,
    private val databaseItemSaver: DatabaseItemSaverRaclette
) {
    suspend fun inTransaction(block: suspend VaultObjectRepository.Transaction.() -> Unit) =
        databaseItemSaver.inTransaction(block)

    fun getItemToSave(vaultItem: VaultItem<*>, backup: XmlTransaction): VaultItemBackupWrapper<SyncObject>? {
        val dataWrapper = VaultItemBackupWrapper(vaultItem, backup)
        return databaseItemSaver.getItemToSave(dataWrapper, DataSaver.SaveRequest.Origin.PERSONAL_SYNC)
    }

    suspend fun getOutgoingTransactions(): List<OutgoingTransaction> {
        val pendingItems = dao.getItemsPendingSyncForType()
        return pendingItems.map { createOutgoingTransaction(it) }
    }

    suspend fun fetchAsOutgoingUpdate(
        uuids: List<String>
    ): List<OutgoingTransaction.Update> {
        return dao.getItemsWithExtraData(uuids).map {
            createOutgoingTransaction(it, SyncState.IN_SYNC_MODIFIED) as OutgoingTransaction.Update
        }
    }

    private fun createOutgoingTransaction(
        it: PendingSyncItemRaclette,
        forcedSyncState: SyncState? = null
    ): OutgoingTransaction {
        val dataIdentifierExtraDataWrapper = it.first
        val vaultItem = dataIdentifierExtraDataWrapper.vaultItem
        val transaction = Transaction(
            vaultItem.uid,
            vaultItem.backupDate ?: Instant.EPOCH
        )
        val syncObjectType = vaultItem.syncObject.objectType
        return when (forcedSyncState ?: it.second) {
            SyncState.IN_SYNC_MODIFIED, SyncState.MODIFIED -> {
                val backupXml = dataIdentifierExtraDataWrapper.backup
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

    suspend fun clearPendingOperations() {
        dao.markAllItemsAsSynced()
    }

    fun getSummary(): Collection<SyncSummaryItem> {
        return dao.getItemsForSummary().map { (id, type, lastBackupDate) ->
            SyncSummaryItem(id, lastBackupDate ?: Instant.EPOCH, type)
        }
    }

    suspend fun applyBackupDate(uuid: List<String>, backupTime: Instant) {
        dao.applyBackupDate(uuid, backupTime)
    }

    suspend fun preparePendingOperations(types: List<SyncObjectType>) {
        dao.markItemsInSync(types)
    }

    suspend fun fetchDuplicate(
        types: List<SyncObjectType>
    ): List<List<OutgoingTransaction.Update>> =
        dao.getDuplicationCandidates(types).map { items ->
            items.map { item ->
                createOutgoingTransaction(item, SyncState.IN_SYNC_MODIFIED) as OutgoingTransaction.Update
            }
        }

    suspend fun flagForDeletion(it: OutgoingTransaction.Update) {
        dao.markItemSyncState(it.identifier, SyncState.DELETED)
    }
}
