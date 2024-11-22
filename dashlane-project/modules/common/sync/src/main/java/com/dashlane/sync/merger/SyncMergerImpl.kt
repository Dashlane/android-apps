package com.dashlane.sync.merger

import com.dashlane.sync.domain.IncomingTransaction
import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.xml.MergeListStrategy
import com.dashlane.sync.xml.mergeInto
import com.dashlane.util.generateUniqueIdentifier
import com.dashlane.xml.XmlData
import com.dashlane.xml.XmlTransaction
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.properties.SyncObjectXml
import com.dashlane.xml.domain.toObject
import com.dashlane.xml.domain.toTransaction
import javax.inject.Inject

class SyncMergerImpl @Inject constructor(
    private val syncConflict: SyncConflict
) : SyncMerger {

    override fun mergeRemoteAndLocalData(
        incomingTransactions: List<IncomingTransaction>,
        outgoingTransactions: List<OutgoingTransaction>
    ): Pair<List<OutgoingTransaction>, List<IncomingTransaction>> {
        val localOperationUUIDs = outgoingTransactions.asSequence()
            .map(OutgoingTransaction::identifier)
            .toSet()

        val transactionUUIDs = incomingTransactions.asSequence()
            .map(IncomingTransaction::identifier)
            .toSet()

        
        val (conflictTodoOnLocalVault, noConflictTodoOnLocalVault) =
            incomingTransactions.partition { localOperationUUIDs.contains(it.identifier) }
        val (conflictTodoOnServer, noConflictTodoOnServer) =
            outgoingTransactions.partition { transactionUUIDs.contains(it.identifier) }

        
        val (resolvedToDoOnLocalVault, resolvedTodoOnServer) = resolveConflicts(
            conflictTodoOnLocalVault,
            conflictTodoOnServer
        )

        
        val toDoOnLocal = noConflictTodoOnLocalVault + resolvedToDoOnLocalVault
        val toDoOnServer = noConflictTodoOnServer + resolvedTodoOnServer

        return Pair(toDoOnServer, toDoOnLocal)
    }

    private fun resolveConflicts(
        toDoOnLocal: List<IncomingTransaction>,
        toDoOnServer: List<OutgoingTransaction>
    ): Pair<List<IncomingTransaction>, List<OutgoingTransaction>> {
        val associatedLocal = toDoOnLocal.associateBy { it.identifier }
        val conflictsMapping = toDoOnServer.map { associatedLocal.getValue(it.identifier) to it }

        val result = conflictsMapping.map { resolveConflict(it.first, it.second) }

        return result.flatMap { it.first } to result.flatMap { it.second }
    }

    private fun resolveConflict(
        pendingLocalOperation: IncomingTransaction,
        pendingRemoteOperation: OutgoingTransaction
    ): Pair<List<IncomingTransaction>, List<OutgoingTransaction>> {
        if (pendingLocalOperation !is IncomingTransaction.Update) {
            
            return listOf(pendingLocalOperation) to listOf()
        }
        if (pendingRemoteOperation !is OutgoingTransaction.Update) {
            
            return listOf<IncomingTransaction>() to listOf(pendingRemoteOperation)
        }

        return when (pendingLocalOperation.syncObjectType) {
            SyncObjectType.DATA_CHANGE_HISTORY -> mergePasswordHistory(pendingLocalOperation, pendingRemoteOperation)
            else -> defaultConflictResolution(pendingLocalOperation, pendingRemoteOperation)
        }
    }


    private fun mergePasswordHistory(
        incomingUpdate: IncomingTransaction.Update,
        outgoingUpdate: OutgoingTransaction.Update
    ): Pair<List<IncomingTransaction>, List<OutgoingTransaction>> {
        val mergeResult = outgoingUpdate.syncObject.toTransaction().node.mergeInto(
            other = incomingUpdate.syncObject.toTransaction().node,
            anonymousListsStrategy = MergeListStrategy.DEDUPLICATE
        )
        val mergedTransaction = XmlTransaction(mergeResult)
        val mergedObject = mergedTransaction.toObject()

        val mergedIncomingTransaction = incomingUpdate.copy(
            syncObject = mergedObject
        )
        val mergedOutgoingTransaction = outgoingUpdate.copy(
            syncObject = mergedObject
        )
        return listOf(mergedIncomingTransaction) to listOf(mergedOutgoingTransaction)
    }

    private fun defaultConflictResolution(
        incomingUpdate: IncomingTransaction.Update,
        outgoingUpdate: OutgoingTransaction.Update
    ): Pair<List<IncomingTransaction>, List<OutgoingTransaction>> {
        if (incomingUpdate.isShared || outgoingUpdate.isShared) {
            
            
            return emptyList<IncomingTransaction>() to listOf(outgoingUpdate)
        }

        val mergedSyncObject = try {
            syncConflict.mergeConflicting(
                outgoingUpdate.syncObjectType,
                outgoingUpdate.backup,
                outgoingUpdate.syncObject,
                incomingUpdate.syncObject
            )
        } catch (e: SyncConflict.DuplicateException) {
            val (incomingDuplicate, outgoingDuplicate) = createDuplicate(outgoingUpdate)
            return listOf(incomingUpdate, incomingDuplicate) to listOf(outgoingDuplicate)
        }
        val mergedOutgoingUpdate = outgoingUpdate.copy(syncObject = mergedSyncObject)
        val mergedIncomingUpdate = incomingUpdate.copy(syncObject = mergedSyncObject)
        return listOf(mergedIncomingUpdate) to listOf(mergedOutgoingUpdate)
    }

    private fun createDuplicate(
        outgoingUpdate: OutgoingTransaction.Update
    ): Pair<IncomingTransaction.Update, OutgoingTransaction.Update> {
        val syncObject = outgoingUpdate.syncObject
        val transactionXml = syncObject.toTransaction()
        val identifier = generateUniqueIdentifier()
        val transactionCopy = outgoingUpdate.transaction.copy(
            identifier = identifier
        )
        val transactionXmlCopy = transactionXml.copy(
            node = transactionXml.node.copy(
                data = transactionXml.node.data + (SyncObjectXml.ID to XmlData.ItemNode(identifier))
            )
        )
        val syncObjectCopy = transactionXmlCopy.toObject(SyncObjectType.forObject(syncObject))
        val isShared = false 
        val incomingCopy = IncomingTransaction.Update(
            transactionCopy,
            syncObjectCopy,
            isShared
        )
        val outgoingCopy = OutgoingTransaction.Update(
            transactionCopy,
            syncObjectCopy,
            syncObjectCopy,
            isShared
        )
        return incomingCopy to outgoingCopy
    }
}
