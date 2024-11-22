package com.dashlane.ui.activities.fragments.vault.list

import android.content.Context
import com.dashlane.feature.home.data.Filter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.vault.summary.SummaryObject

interface VaultList {
    interface ViewModel {
        fun onRefresh()
    }

    interface DataProvider {
        fun syncData()
        suspend fun generateViewTypeProviderList(
            items: List<SummaryObject>,
            filter: Filter,
            sortMode: VaultListDataProvider.UnboundedListSortMode,
            context: Context
        ): List<DashlaneRecyclerAdapter.ViewTypeProvider>

        suspend fun generateViewTypeProviderHighlightList(
            items: List<SummaryObject>,
            filter: Filter,
            sortMode: VaultListDataProvider.BoundedListSortMode,
            context: Context
        ): List<DashlaneRecyclerAdapter.ViewTypeProvider>
    }
}