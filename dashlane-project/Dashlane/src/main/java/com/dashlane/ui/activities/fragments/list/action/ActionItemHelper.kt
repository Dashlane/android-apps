package com.dashlane.ui.activities.fragments.list.action

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import com.dashlane.R

class ActionItemHelper {

    fun createNewActionItem(
        context: Context,
        icon: Drawable?,
        iconWithTint: Boolean,
        title: String,
        tintColor: Int?,
        action: () -> Unit
    ): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_action_bottom_sheet_dialog, null)

        val titleView = view.findViewById<TextView>(R.id.action_title)
        titleView.text = title
        tintColor?.let { titleView.setTextColor(tintColor) }
        val actionIcon = view.findViewById<ImageView>(R.id.action_icon)
        if (!iconWithTint) {
            
            ImageViewCompat.setImageTintList(actionIcon, null)
        } else if (tintColor != null) {
            ImageViewCompat.setImageTintList(actionIcon, ColorStateList.valueOf(tintColor))
        }
        actionIcon.setImageDrawable(icon)

        view.setOnClickListener { action.invoke() }

        return view
    }
}