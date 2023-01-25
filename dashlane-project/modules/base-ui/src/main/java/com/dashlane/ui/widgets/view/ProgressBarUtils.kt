package com.dashlane.ui.widgets.view

import android.graphics.drawable.LayerDrawable
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import com.dashlane.ui.R



fun ProgressBar.tintProgressDrawable(
    @ColorInt
    foregroundTintColor: Int,
    @ColorInt
    backgroundTintColor: Int = context.getColor(R.color.border_neutral_quiet_idle)
) {
    val progressDrawable = progressDrawable ?: return
    if (progressDrawable !is LayerDrawable) return

    
    
    if (progressDrawable.numberOfLayers >= 3) {
        progressDrawable.getDrawable(2).setTint(foregroundTintColor)
    }

    
    progressDrawable.getDrawable(0).setTint(backgroundTintColor)
}