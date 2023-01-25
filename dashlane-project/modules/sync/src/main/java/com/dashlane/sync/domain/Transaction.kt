package com.dashlane.sync.domain

import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.objectType
import java.time.Instant

data class Transaction(
    val identifier: String,
    val date: Instant
)

sealed class IncomingTransaction {

    val identifier
        get() = transaction.identifier
    val date
        get() = transaction.date

    abstract val transaction: Transaction
    abstract val syncObjectType: SyncObjectType

    data class Update(
        override val transaction: Transaction,
        val syncObject: SyncObject,
        val isShared: Boolean = false
    ) : IncomingTransaction() {
        override val syncObjectType: SyncObjectType
            get() = syncObject.objectType
    }

    data class Delete(
        override val transaction: Transaction,
        override val syncObjectType: SyncObjectType
    ) : IncomingTransaction()
}

sealed class OutgoingTransaction {

    val identifier
        get() = transaction.identifier
    val date
        get() = transaction.date

    abstract val transaction: Transaction
    abstract val syncObjectType: SyncObjectType

    data class Update(
        override val transaction: Transaction,
        val syncObject: SyncObject,
        val backup: SyncObject,
        val isShared: Boolean = false
    ) : OutgoingTransaction() {
        override val syncObjectType: SyncObjectType
            get() = syncObject.objectType
    }

    data class Delete(
        override val transaction: Transaction,
        override val syncObjectType: SyncObjectType
    ) : OutgoingTransaction()
}
