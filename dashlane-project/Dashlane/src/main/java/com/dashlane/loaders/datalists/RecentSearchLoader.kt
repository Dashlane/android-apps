package com.dashlane.loaders.datalists

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.storage.userdata.accessor.FrequentSearch
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

class RecentSearchLoader(
    private val frequentSearch: FrequentSearch,
    private val coroutineScope: CoroutineScope,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) {
    private var all: Deferred<List<SummaryObject>>? = null

    constructor(scope: CoroutineScope, coroutineContext: CoroutineContext = Dispatchers.IO) : this(
        SingletonProvider.getMainDataAccessor().getFrequentSearch(),
        scope,
        coroutineContext
    )

    fun reloadData() {
        all = coroutineScope.async(coroutineContext) {
            frequentSearch.getLastSearchedItems(max = 100)
        }
    }

    suspend fun get(): List<SummaryObject>? {
        if (all == null) {
            reloadData()
        }
        return all?.await()
    }
}