package com.dashlane.sync.domain

import com.dashlane.server.api.endpoints.sync.SyncDownloadTransaction

internal fun List<SyncDownloadTransaction>.filterSupportedTransactions(
    @SupportedTransactionsFilter transactionFilter: List<String>?,
): List<SyncDownloadTransaction> {
    return transactionFilter?.let {
        this.filter { transaction ->
            transaction.type in transactionFilter
        }
    } ?: this
}