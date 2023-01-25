package com.dashlane.sync.merger

import com.dashlane.sync.domain.IncomingTransaction
import com.dashlane.sync.domain.OutgoingTransaction

interface SyncMerger {

    

    fun mergeRemoteAndLocalData(
        incomingTransactions: List<IncomingTransaction>,
        outgoingTransactions: List<OutgoingTransaction>
    ): Pair<List<OutgoingTransaction>, List<IncomingTransaction>>
}