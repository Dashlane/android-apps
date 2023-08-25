package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import android.content.Context
import android.view.View
import android.widget.CheckBox
import com.dashlane.R
import com.dashlane.ui.activities.fragments.list.ItemWrapperViewHolder
import com.dashlane.ui.activities.fragments.list.action.ListItemAction
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemDoubleWrapper
import com.dashlane.ui.activities.fragments.list.wrapper.VaultItemWrapper
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.ViewTypeUtils
import com.dashlane.vault.summary.SummaryObject

class ItemWrapperSelectable<D : SummaryObject>(itemWrapper: VaultItemWrapper<D>) :
    VaultItemDoubleWrapper<D>(itemWrapper) {
    var isSelect = false

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>?> = VIEW_TYPE

    override fun getSpanSize(spanCount: Int): Int = 1

    override fun isItemTheSame(item: VaultItemWrapper<out SummaryObject>): Boolean = false

    override fun isContentTheSame(item: VaultItemWrapper<out SummaryObject>): Boolean {
        return item.itemObject.id == itemObject.id
    }

    override fun getListItemActions(): List<ListItemAction> = emptyList()

    class ItemViewHolder(itemView: View) : ItemWrapperViewHolder(itemView) {
        override fun updateView(context: Context, item: VaultItemWrapper<out SummaryObject>?) {
            super.updateView(context, item)
            if (item is ItemWrapperSelectable<out SummaryObject>) {
                findViewByIdEfficient<CheckBox>(R.id.checkbox)?.let { checkBox ->
                    checkBox.visibility = View.VISIBLE
                    checkBox.isChecked = item.isSelect
                }
            }
        }
    }

    companion object {
        private val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<VaultItemWrapper<out SummaryObject>?> =
            DashlaneRecyclerAdapter.ViewType(
                ViewTypeUtils.DEFAULT_ITEM_WRAPPER_VIEW_TYPE.layoutResId,
                ItemViewHolder::class.java
            )
    }
}