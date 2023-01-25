package com.dashlane.core.history

import com.dashlane.storage.userdata.accessor.DataChangeHistoryQuery

interface DataChangeHistoryQueryProvider {
    fun get(): DataChangeHistoryQuery
}