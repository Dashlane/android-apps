package com.dashlane.storage.userdata.accessor

import com.dashlane.lock.LockHelper
import com.dashlane.storage.DataStorageProvider
import com.dashlane.storage.userdata.accessor.filter.CounterFilter
import dagger.Lazy
import javax.inject.Inject



class DataCounterRacletteImpl @Inject constructor(
    private val dataStorageProvider: Lazy<DataStorageProvider>,
    private val lockHelper: LockHelper
) : DataCounter {

    private val genericDataQuery: GenericDataQuery
        get() = dataStorageProvider.get().genericDataQuery

    override fun count(filter: CounterFilter): Int {
        if (lockHelper.forbidDataAccess(filter)) return DataCounter.NO_COUNT
        return genericDataQuery.queryAll(filter).count()
    }
}