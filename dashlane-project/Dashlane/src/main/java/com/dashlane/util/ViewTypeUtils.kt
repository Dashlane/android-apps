@file:JvmName("ViewTypeUtils")

package com.dashlane.util

import com.dashlane.R
import com.dashlane.ui.activities.fragments.list.ItemWrapperViewHolder
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.vault.summary.SummaryObject

object ViewTypeUtils {
    

    @JvmField
    val DEFAULT_ITEM_WRAPPER_VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>> =
        DashlaneRecyclerAdapter.ViewType(R.layout.item_dataidentifier, ItemWrapperViewHolder::class.java)
}