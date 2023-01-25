package com.dashlane.item.subview.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.dashlane.R



object ExpandableListProvider {
    fun create(
        context: Context,
        list: List<String>,
        summary: String,
        showListListener: (List<String>) -> Unit = { }
    ): View {
        val layout = LayoutInflater.from(context).inflate(R.layout.item_collapsible_text_list, null)
        val itemHeader = layout.findViewById<LinearLayout>(R.id.item_header_layout)
        val itemTitle = layout.findViewById<TextView>(R.id.item_title)
        val itemExpandArrow = layout.findViewById<ImageView>(R.id.item_expand_arrow)
        val itemTextList = layout.findViewById<TextView>(R.id.item_text_list)

        itemTitle.text = summary
        itemTextList.text = list.joinToString("\n")

        itemHeader.setOnClickListener {
            val isExpanded = itemTextList.visibility == View.VISIBLE
            if (isExpanded) {
                itemExpandArrow.animate().rotation(0f)
                itemTextList.visibility = View.GONE
                itemExpandArrow.contentDescription =
                    context.getString(R.string.and_accessibility_action_expand)
            } else {
                showListListener.invoke(list)
                itemExpandArrow.animate().rotation(-180f)
                itemTextList.visibility = View.VISIBLE
                itemExpandArrow.contentDescription =
                    context.getString(R.string.and_accessibility_action_collapse)
            }
        }

        return layout
    }
}