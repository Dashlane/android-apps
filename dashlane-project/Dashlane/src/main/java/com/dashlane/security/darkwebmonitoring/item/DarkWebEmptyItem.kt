package com.dashlane.security.darkwebmonitoring.item

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.empty.EmptyScreenConfiguration
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

data class DarkWebEmptyItem(private val emptyScreenConfiguration: EmptyScreenConfiguration) :
    DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val v: View) : EfficientViewHolder<DarkWebEmptyItem>(v) {
        override fun updateView(context: Context, item: DarkWebEmptyItem?) {
            `object`!!.emptyScreenConfiguration.configureWithView(v)
        }
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.empty_screen_generic,
            ViewHolder::class.java
        )
    }
}