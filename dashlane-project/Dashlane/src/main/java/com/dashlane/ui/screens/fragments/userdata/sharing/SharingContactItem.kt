package com.dashlane.ui.screens.fragments.userdata.sharing

import android.content.Context
import android.view.View
import com.dashlane.R
import com.dashlane.design.component.compat.view.ThumbnailView
import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter.MultiColumnViewTypeProvider
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.util.isValidEmail
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

abstract class SharingContactItem(
    val context: Context,
    val line1: String,
    private val updateActionView: EfficientViewHolder<SharingContactItem>.() -> Unit = {}
) : MultiColumnViewTypeProvider, DiffUtilComparator<SharingContactItem> {

    abstract fun getLine2(): String
    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = VIEW_TYPE

    override fun getSpanSize(spanCount: Int): Int = 1

    override fun isItemTheSame(item: SharingContactItem): Boolean = line1 == item.line1

    override fun isContentTheSame(item: SharingContactItem): Boolean =
        isItemTheSame(item)

    class ItemViewHolder(itemView: View) : EfficientViewHolder<SharingContactItem>(itemView) {
        override fun updateView(context: Context, sharingContactItem: SharingContactItem?) {
            sharingContactItem ?: return
            findViewByIdEfficient<ThumbnailView>(R.id.icon)?.let { thumbnailView ->
                if (sharingContactItem.line1.isValidEmail()) {
                    thumbnailView.thumbnailType = ThumbnailViewType.USER_SINGLE.value
                    thumbnailView.thumbnailUrl = sharingContactItem.line1
                } else {
                    thumbnailView.thumbnailType = ThumbnailViewType.ICON.value
                    thumbnailView.iconRes = R.drawable.ic_group_outlined
                }
            }

            setText(R.id.item_line1, sharingContactItem.line1)
            setText(R.id.item_line2, sharingContactItem.getLine2())

            setVisibility(R.id.action, View.GONE)
            apply(sharingContactItem.updateActionView)
        }
    }

    companion object {
        private val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<out SharingContactItem> =
            DashlaneRecyclerAdapter.ViewType(
                R.layout.list_item_content_sharing_by_user_layout,
                ItemViewHolder::class.java
            )

        fun comparator(): Comparator<SharingContactItem> = compareBy { it.line1 }
    }
}