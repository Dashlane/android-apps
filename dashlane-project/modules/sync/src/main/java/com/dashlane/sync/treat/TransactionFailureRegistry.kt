package com.dashlane.sync.treat

import com.dashlane.sync.SyncScope
import javax.inject.Inject



@SyncScope
class TransactionFailureRegistry @Inject constructor() {

    private val summaryItems = mutableSetOf<SyncSummaryItem>()

    fun getAll(): Set<SyncSummaryItem> =
        summaryItems

    fun register(summaryItem: SyncSummaryItem) {
        summaryItems += summaryItem
    }
}