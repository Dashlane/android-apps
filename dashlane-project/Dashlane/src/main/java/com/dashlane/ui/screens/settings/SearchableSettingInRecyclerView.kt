package com.dashlane.ui.screens.settings

import android.content.Context
import android.text.Spannable
import android.view.View
import androidx.core.text.toSpannable
import com.dashlane.R
import com.dashlane.search.FieldType
import com.dashlane.search.SearchField
import com.dashlane.search.SearchableSettingItem
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.toHighlightedSpannable
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder



class SearchableSettingInRecyclerView(
    val item: SearchableSettingItem,
    private val targetText: String? = null,
    private val matchField: SearchField<*>? = null,
) : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(view: View) : EfficientViewHolder<SearchableSettingInRecyclerView>(view) {

        override fun updateView(context: Context, item: SearchableSettingInRecyclerView?) {
            val display = item?.item

            val description = display?.getSettingDescription()?.toSpannable()

            setTitle(display, item, context)
            setDescription(description, item, context)
            setThirdLine(display)
        }

        private fun setThirdLine(display: SearchableSettingItem?) {
            setText(R.id.item_third_line, display?.getPathDisplay() ?: display?.getSettingTitle())
        }

        private fun setTitle(
            display: SearchableSettingItem?,
            item: SearchableSettingInRecyclerView?,
            context: Context
        ) {
            val title = display?.getSettingTitle()?.toSpannable()
            if (item?.matchField?.fieldType == FieldType.PRIMARY && item.targetText != null) {
                title?.toHighlightedSpannable(
                    item.targetText,
                    context.getThemeAttrColor(R.attr.colorSecondary)
                )
            }
            setText(R.id.item_title, title)
        }

        private fun setDescription(
            description: Spannable?,
            item: SearchableSettingInRecyclerView?,
            context: Context
        ) {
            if (description.isNullOrEmpty()) {
                setVisibility(R.id.item_subtitle, View.GONE)
            } else {
                setVisibility(R.id.item_subtitle, View.VISIBLE)
                if (item?.matchField?.fieldType == FieldType.SECONDARY && item.targetText != null) {
                    description.toHighlightedSpannable(
                        item.targetText,
                        context.getThemeAttrColor(R.attr.colorSecondary)
                    )
                }
                setText(R.id.item_subtitle, description)
            }
        }
    }

    companion object {

        val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<SearchableSettingInRecyclerView> =
            DashlaneRecyclerAdapter.ViewType(
                R.layout.item_searcheable_setting, ViewHolder::class.java
            )
    }
}