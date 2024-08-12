package com.dashlane.ui.activities.fragments.vault.list

import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.home.vaultlist.Filter

data class VaultListData(
    val filter: Filter,
    val list: List<DashlaneRecyclerAdapter.ViewTypeProvider> = emptyList(),
    val isLoading: Boolean = false
)