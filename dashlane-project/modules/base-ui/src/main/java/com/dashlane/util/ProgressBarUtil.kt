package com.dashlane.util

import android.graphics.drawable.LayerDrawable
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

fun ProgressBar.setProgressDrawablePrimaryTrack(@DrawableRes drawableResId: Int): Boolean {
    
    
    val progressDrawable = progressDrawable as? LayerDrawable
    val newDrawable = AppCompatResources.getDrawable(context, drawableResId) ?: return false
    progressDrawable?.setDrawableByLayerId(android.R.id.progress, newDrawable)
    return progressDrawable?.findIndexByLayerId(android.R.id.progress)?.let { it >= 0 } ?: false
}