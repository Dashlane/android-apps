package com.dashlane.notificationcenter.view

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

data class HeaderItem(
    val section: ActionItemSection,
    var count: Int?,
    val seeAllListener: (ActionItemSection) -> Unit
) : DashlaneRecyclerAdapter.MultiColumnViewTypeProvider, DiffUtilComparator<HeaderItem> {
    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> {
        return VIEW_TYPE
    }

    override fun getSpanSize(spanCount: Int): Int = spanCount

    override fun isItemTheSame(item: HeaderItem) = section == item.section

    override fun isContentTheSame(item: HeaderItem) = this == item

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_actionitem_header,
            ViewHolder::class.java
        )
    }

    class ViewHolder(view: View) : EfficientViewHolder<HeaderItem>(view) {
        override fun updateView(context: Context, item: HeaderItem?) {
            item ?: return
            setText(R.id.title, context.getString(item.section.titleRes))
            val seeAllText = if (item.count != null) {
                context.getString(R.string.section_header_see_all_button_with_count, item.count)
            } else {
                context.getString(R.string.section_header_see_all_button)
            }
            setText(
                R.id.see_all_button,
                seeAllText
            )
            findViewByIdEfficient<View>(R.id.see_all_button)?.setOnClickListener { item.seeAllListener(item.section) }
        }
    }
}