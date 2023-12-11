package com.dashlane.ui.activities.fragments.vault

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.ui.activities.fragments.list.ItemWrapperViewHolder
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.summary.SummaryObject
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class VaultItemViewTypeProvider(
    val summaryObject: SummaryObject,
    val itemListContext: ItemListContext,
    itemWrapperProvider: ItemWrapperProvider
) : DashlaneRecyclerAdapter.MultiColumnViewTypeProvider {
    val itemWrapper = itemWrapperProvider(summaryObject, itemListContext)

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = DEFAULT_ITEM_WRAPPER_VIEW_TYPE

    override fun getSpanSize(spanCount: Int): Int = 1

    companion object {
        val DEFAULT_ITEM_WRAPPER_VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<VaultItemViewTypeProvider> =
            DashlaneRecyclerAdapter.ViewType(R.layout.item_dataidentifier, ItemViewHolder::class.java)
    }

    class ItemViewHolder(view: View) : EfficientViewHolder<VaultItemViewTypeProvider>(view) {
        private val itemWrapperViewHolder: ItemWrapperViewHolder = ItemWrapperViewHolder(view)

        override fun updateView(context: Context, vaultItemViewTypeProvider: VaultItemViewTypeProvider?) {
            itemWrapperViewHolder.updateView(context, vaultItemViewTypeProvider?.itemWrapper)
        }
    }
}