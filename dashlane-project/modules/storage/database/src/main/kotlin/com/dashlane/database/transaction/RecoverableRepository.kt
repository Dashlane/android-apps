package com.dashlane.database.transaction

internal interface RecoverableRepository {
    suspend fun applyPendingTransaction()
}