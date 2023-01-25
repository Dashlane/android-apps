package com.dashlane.security.identitydashboard.item

import android.content.Context
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder



data class IdentityDashboardHeader(private val text: String) : IdentityDashboardItem {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val v: View) : EfficientViewHolder<IdentityDashboardHeader>(v) {
        override fun updateView(context: Context, item: IdentityDashboardHeader?) {
            (view as TextView).text = item?.text
        }
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType<IdentityDashboardHeader>(
            R.layout.item_header_big,
            ViewHolder::class.java
        )
    }
}