package com.dashlane.database.transaction

import com.dashlane.database.DatabaseException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout

internal interface CommonTransaction {
    suspend fun closeTransaction(): TransactionResult?

    suspend fun applyTransaction()
}

internal suspend fun CommonTransaction.performTransaction(
    mutex: Mutex,
    timeoutMillis: Long,
    block: suspend () -> Unit,
): TransactionResult? = transaction(this, mutex, timeoutMillis, block)

internal suspend fun CommonTransaction.performPendingTransaction(
    mutex: Mutex,
) = applyPendingTransaction(this, mutex)

private suspend fun transaction(
    transaction: CommonTransaction,
    mutex: Mutex,
    timeoutMillis: Long,
    block: suspend () -> Unit,
): TransactionResult? {
    return try {
        withTimeout(timeoutMillis) {
            mutex.lock(transaction)
        }
        block()
        transaction.closeTransaction()
    } catch (e: Exception) {
        throw DatabaseException(message = "transaction fails", cause = e)
    } finally {
        try {
            mutex.unlock(transaction)
        } catch (e: IllegalStateException) {
            
        }
    }
}

private suspend fun applyPendingTransaction(
    transaction: CommonTransaction,
    mutex: Mutex,
) {
    if (mutex.isLocked) {
        throw IllegalStateException("Should not happen. ApplyPendingTransaction but transaction is locked")
    }
    try {
        mutex.lock(transaction)
        transaction.applyTransaction()
    } catch (e: Exception) {
        throw DatabaseException(message = "applyPendingTransaction fails", cause = e)
    } finally {
        try {
            mutex.unlock(transaction)
        } catch (e: IllegalStateException) {
            
        }
    }
}