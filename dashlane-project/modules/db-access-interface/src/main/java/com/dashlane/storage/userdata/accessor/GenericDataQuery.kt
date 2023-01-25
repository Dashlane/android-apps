package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.BaseFilter
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.vault.summary.SummaryObject



interface GenericDataQuery : DataQuery<SummaryObject, BaseFilter> {
    override fun createFilter(): GenericFilter
}