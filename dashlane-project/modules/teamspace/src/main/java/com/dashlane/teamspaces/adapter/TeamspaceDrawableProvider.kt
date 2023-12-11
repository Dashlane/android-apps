package com.dashlane.teamspaces.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DimenRes
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.teamspaces.R
import com.dashlane.teamspaces.model.Teamspace

object TeamspaceDrawableProvider {
    @JvmStatic
    fun getIcon(context: Context, teamspace: Teamspace, @DimenRes dimenResId: Int = 0): Drawable? {
        val drawable = when (teamspace.type) {
            Teamspace.Type.COMBINED -> AppCompatResources.getDrawable(context, R.drawable.all_spaces_icon)
            else -> TeamspaceIconDrawable.newInstance(
                context,
                teamspace.displayLetter,
                teamspace.colorInt
            )
        }
        if (drawable != null && dimenResId != 0) {
            val size = context.resources.getDimensionPixelSize(dimenResId)
            drawable.setBounds(0, 0, size, size)
            if (drawable is TeamspaceIconDrawable) {
                drawable.setSize(size)
            }
        }
        return drawable
    }
}