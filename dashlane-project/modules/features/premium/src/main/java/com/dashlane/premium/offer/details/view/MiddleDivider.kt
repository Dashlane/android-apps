package com.dashlane.premium.offer.details.view

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

internal class MiddleDivider(resources: Resources, @DimenRes dimenRes: Int) : RecyclerView.ItemDecoration() {

    private val offsetSize = resources.getDimensionPixelSize(dimenRes)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if (position != 0) {
            outRect.top += offsetSize
        }
    }
}