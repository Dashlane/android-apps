package com.dashlane.ui.fragments

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import com.dashlane.ui.widgets.view.tintProgressDrawable

class StrengthBarAnimation(
    private val progressBar: ProgressBar,
    private val from: Float,
    private val to: Float,
    private val colorTo: Int
) : Animation() {
    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        super.applyTransformation(interpolatedTime, t)
        val value = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()
        progressBar.tintProgressDrawable(colorTo)
    }
}