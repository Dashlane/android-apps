package com.dashlane.storage.userdata.accessor

import com.dashlane.lock.LockHelper
import com.dashlane.storage.userdata.accessor.filter.CounterFilter
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataCounterRacletteImpl @Inject constructor(
    private val genericDataQuery: Lazy<GenericDataQuery>,
    private val lockHelper: LockHelper
) : DataCounter {

    override fun count(filter: CounterFilter): Int {
        if (lockHelper.forbidDataAccess(filter)) return DataCounter.NO_COUNT
        return genericDataQuery.get().queryAll(filter).count()
    }
}