package com.dashlane.util

import android.content.res.Resources
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.StringRes

interface Toaster {
    @IntDef(Toast.LENGTH_LONG, Toast.LENGTH_SHORT)
    @IntRange(from = Toast.LENGTH_SHORT.toLong())
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Duration

    fun show(text: CharSequence?, @Duration duration: Int, position: Position = Position.BOTTOM)

    fun show(text: CharSequence?, @Duration duration: Int)

    @Throws(Resources.NotFoundException::class)
    fun show(@StringRes resId: Int, @Duration duration: Int, position: Position = Position.BOTTOM)

    fun show(@StringRes resId: Int, @Duration duration: Int)

    enum class Position(val gravity: Int) {
        TOP(Gravity.TOP),
        CENTER(Gravity.CENTER),
        BOTTOM(Gravity.BOTTOM)
    }
}