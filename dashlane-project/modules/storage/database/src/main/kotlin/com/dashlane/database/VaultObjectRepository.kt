package com.dashlane.database

import com.dashlane.database.transaction.TransactionResult
import com.dashlane.sync.VaultItemBackupWrapper
import com.dashlane.vault.model.VaultItem

interface VaultObjectRepository {
    companion object {
        internal const val DEFAULT_TIMEOUT_MILLIS = 30_000L
    }

    suspend fun get(id: Id): VaultItem<*>?

    suspend fun transaction(
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
        block: suspend Transaction.() -> Unit,
    ): TransactionResult?

    interface Transaction {
        fun update(vaultObject: VaultItem<*>)

        fun delete(id: Id)

        fun update(data: VaultItemBackupWrapper<*>)
    }
}