package com.dashlane.security.darkwebmonitoring.item

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class DarkWebEmailPlaceholderItem : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val v: View) : EfficientViewHolder<DarkWebEmailPlaceholderItem>(v) {

        override fun updateView(context: Context, item: DarkWebEmailPlaceholderItem?) = Unit

        override fun isClickable() = false

        override fun isLongClickable() = false
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_dark_web_email_placeholder,
            ViewHolder::class.java
        )
    }
}