package com.dashlane.ui.activities.fragments.vault.list

import android.content.Context
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.home.vaultlist.Filter

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