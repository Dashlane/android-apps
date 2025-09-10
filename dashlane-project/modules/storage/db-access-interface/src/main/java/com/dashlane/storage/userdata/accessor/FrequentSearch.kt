package com.dashlane.storage.userdata.accessor

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType

interface FrequentSearch {
    suspend fun markedAsSearched(itemId: String, syncObjectType: SyncObjectType)

    suspend fun getLastSearchedItems(max: Int = 10): List<SummaryObject>
}