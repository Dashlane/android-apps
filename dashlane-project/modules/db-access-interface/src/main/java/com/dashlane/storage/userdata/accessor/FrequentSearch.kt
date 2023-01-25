package com.dashlane.storage.userdata.accessor

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface FrequentSearch {
    

    suspend fun markedAsSearched(itemId: String, syncObjectType: SyncObjectType)

    

    fun getFrequentlySearchedItems(max: Int = 10): List<SummaryObject>

    

    fun getLastSearchedItems(max: Int = 10): List<SummaryObject>
}

@OptIn(DelicateCoroutinesApi::class)
fun FrequentSearch.markedAsSearchedAsync(itemId: String, syncObjectType: SyncObjectType) = GlobalScope.launch {
    markedAsSearched(itemId, syncObjectType)
}