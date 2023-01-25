package com.dashlane.ui.activities.fragments.list

import android.view.View
import com.dashlane.R



class ItemWrapperOneColViewHolder(itemView: View) : ItemWrapperViewHolder(itemView) {
    init {
        findViewByIdEfficient<View>(R.id.item_icon)?.let { iconView ->
            val layoutParams = iconView.layoutParams
            layoutParams.height = itemView.resources.getDimensionPixelSize(R.dimen.item_icon_height)
            layoutParams.width = itemView.resources.getDimensionPixelSize(R.dimen.item_icon_width)
            iconView.layoutParams = layoutParams
        }
    }
}