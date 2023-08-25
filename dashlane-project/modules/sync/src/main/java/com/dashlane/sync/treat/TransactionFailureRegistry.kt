package com.dashlane.sync.treat

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionFailureRegistry @Inject constructor() {

    private val summaryItems = mutableSetOf<SyncSummaryItem>()

    fun getAll(): Set<SyncSummaryItem> =
        summaryItems

    fun register(summaryItem: SyncSummaryItem) {
        summaryItems += summaryItem
    }
}