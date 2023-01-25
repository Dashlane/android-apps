package com.dashlane.security.identitydashboard.item

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder



class IdentityDashboardSeparatorItem : IdentityDashboardItem {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val v: View) : EfficientViewHolder<IdentityDashboardSeparatorItem>(v) {
        override fun updateView(context: Context, item: IdentityDashboardSeparatorItem?) {
            
        }
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType<IdentityDashboardSeparatorItem>(
            R.layout.item_id_separator,
            ViewHolder::class.java
        )
    }
}