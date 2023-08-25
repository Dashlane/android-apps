package com.dashlane.security.darkwebmonitoring.item

import android.content.Context
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

data class DarkWebHeaderItem(private val text: String) : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val v: View) : EfficientViewHolder<DarkWebHeaderItem>(v) {
        override fun updateView(context: Context, item: DarkWebHeaderItem?) {
            (view as TextView).text = item?.text
        }
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType<DarkWebHeaderItem>(
            R.layout.item_header,
            ViewHolder::class.java
        )
    }
}