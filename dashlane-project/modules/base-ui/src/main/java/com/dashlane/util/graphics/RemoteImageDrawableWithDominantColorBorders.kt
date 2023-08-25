package com.dashlane.util.graphics

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class RemoteImageDrawableWithDominantColorBorders(
    context: Context,
    @ColorInt backgroundColor: Int,
    private val dominantColorListener: (Int) -> Unit = { _ -> }
) : RemoteImageRoundRectDrawable(context, backgroundColor) {
    override fun setImage(drawable: Drawable) {
        super.setImage(drawable)
        dominantColorListener.invoke(getDominantColor(drawable))
    }
}
