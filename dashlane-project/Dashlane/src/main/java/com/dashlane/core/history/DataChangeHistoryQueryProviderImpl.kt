package com.dashlane.core.history

import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import javax.inject.Inject

class DataChangeHistoryQueryProviderImpl @Inject constructor(
    private val mainDataAccessor: dagger.Lazy<MainDataAccessor>
) : DataChangeHistoryQueryProvider {
    override fun get(): DataChangeHistoryQuery = mainDataAccessor.get().getDataChangeHistoryQuery()
}