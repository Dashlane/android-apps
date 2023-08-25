package com.dashlane.notificationcenter.view

import android.content.Context
import android.view.View

import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class ActionItemEmptyItemViewHolder(itemView: View) :
    EfficientViewHolder<DashlaneRecyclerAdapter.MultiColumnViewTypeProvider>(itemView) {

    override fun updateView(context: Context, `object`: DashlaneRecyclerAdapter.MultiColumnViewTypeProvider?) {
        
    }

    companion object {

        val ITEM: DashlaneRecyclerAdapter.MultiColumnViewTypeProvider =
            object : DashlaneRecyclerAdapter.MultiColumnViewTypeProvider {

                override fun getSpanSize(spanCount: Int) = spanCount

                override fun getViewType() =
                    DashlaneRecyclerAdapter.ViewType<DashlaneRecyclerAdapter.MultiColumnViewTypeProvider>(
                        R.layout.item_empty_actionitem,
                        ActionItemEmptyItemViewHolder::class.java
                    )
            }
    }
}