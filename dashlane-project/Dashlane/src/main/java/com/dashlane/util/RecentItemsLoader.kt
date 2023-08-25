package com.dashlane.util

import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.ui.activities.fragments.vault.Filter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.mostRecentAccessTime
import javax.inject.Inject

class RecentItemsLoader @Inject constructor(
    private val genericDataQuery: GenericDataQuery
) {
    fun loadRecentItems(maxResult: Int, ignoreUserLock: Boolean = false): List<SummaryObject> {
        val filter = genericFilter {
            specificDataType(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)
            forCurrentSpace()
            if (ignoreUserLock) ignoreUserLock()
        }

        val all = genericDataQuery.queryAll(filter)
        return all.filter { it.mostRecentAccessTime != null }
            .sortedByDescending { it.mostRecentAccessTime }
            .take(maxResult)
    }
}
