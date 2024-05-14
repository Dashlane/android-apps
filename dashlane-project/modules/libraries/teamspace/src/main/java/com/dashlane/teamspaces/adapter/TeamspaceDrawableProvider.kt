package com.dashlane.teamspaces.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DimenRes
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.teamspaces.R
import com.dashlane.teamspaces.model.SpaceColor
import com.dashlane.teamspaces.model.TeamSpace

object TeamspaceDrawableProvider {
    @JvmStatic
    fun getIcon(context: Context, teamspace: TeamSpace, @DimenRes dimenResId: Int = 0): Drawable? {
        val drawable = when (teamspace) {
            is TeamSpace.Combined -> AppCompatResources.getDrawable(context, R.drawable.all_spaces_icon)
            else -> TeamspaceIconDrawable(
                context = context,
                displayLetter = teamspace.displayLetter,
                color = when (val color = teamspace.color) {
                    is SpaceColor.FixColor -> context.getColor(color.colorRes)
                    is SpaceColor.TeamColor -> color.color
                }
            )
        }
        if (drawable != null && dimenResId != 0) {
            val size = context.resources.getDimensionPixelSize(dimenResId)
            drawable.setBounds(0, 0, size, size)
            if (drawable is TeamspaceIconDrawable) {
                drawable.size = size
            }
        }
        return drawable
    }
}