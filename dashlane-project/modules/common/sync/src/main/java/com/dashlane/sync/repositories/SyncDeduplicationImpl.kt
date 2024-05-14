package com.dashlane.sync.repositories

import com.dashlane.sync.domain.OutgoingTransaction
import com.dashlane.sync.vault.SyncVault
import javax.inject.Inject

class SyncDeduplicationImpl @Inject constructor() : SyncDeduplication {
    override suspend fun performDeduplication(vault: SyncVault): Int =
        vault.getDeduplicationCandidates()
            .map { list ->
                getDuplicates(list)
                    .onEach { vault.flagForDeletion(it) }
                    .size
            }
            .sum()

    private fun getDuplicates(list: List<OutgoingTransaction.Update>) =
        list.groupBy { it.syncObject.userContentData }
            .values.filter { it.size > 1 }
            .flatMap { dropMostRecent(it) }

    private fun dropMostRecent(list: List<OutgoingTransaction.Update>): List<OutgoingTransaction.Update> =
        list - list.maxByOrNull(OutgoingTransaction.Update::date)!!
}