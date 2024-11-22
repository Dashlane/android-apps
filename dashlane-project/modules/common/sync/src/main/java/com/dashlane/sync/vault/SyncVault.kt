package com.dashlane.sync.vault

import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.treat.SyncSummaryItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import kotlin.reflect.KClass

interface SyncVault {

    var lastSyncTime: Instant?

    suspend fun inTransaction(block: suspend TransactionScope.() -> Unit)

    suspend fun prepareOutgoingOperations(types: List<SyncObjectType>)

    suspend fun getOutgoingTransactions(types: List<SyncObjectType>): List<OutgoingTransaction>

    suspend fun clearOutgoingOperations(types: List<SyncObjectType>)

    suspend fun getSummary(syncObjectTypes: List<SyncObjectType>): Collection<SyncSummaryItem>

    suspend fun fetchAsOutgoingUpdate(descriptors: Collection<SyncObjectDescriptor>): List<OutgoingTransaction.Update>

    suspend fun applyBackupDate(descriptors: Collection<SyncObjectDescriptor>, backupTimeMillis: Instant)

    suspend fun getDeduplicationCandidates(): List<List<OutgoingTransaction.Update>>

    suspend fun flagForDeletion(it: OutgoingTransaction.Update)

    interface TransactionScope {

        fun insertOrUpdateForSync(
            identifier: String,
            value: SyncObject,
            backupTimeMillis: Long
        )

        fun deleteForSync(kClass: KClass<out SyncObject>, uuid: String)
    }
}