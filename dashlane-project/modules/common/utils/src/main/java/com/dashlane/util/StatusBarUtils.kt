@file:JvmName("StatusBarUtils")

package com.dashlane.util

import android.app.Activity
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.drawerlayout.widget.DrawerLayout

@get:ColorInt
@setparam:ColorInt
var Activity.statusBarColor: Int
    get() = window.statusBarColor
    set(value) {
        window.statusBarColor = value
    }

fun Activity.setStatusBarColor(@ColorInt color: Int, drawerLayout: DrawerLayout?) {
    if (drawerLayout != null) {
        drawerLayout.setStatusBarBackgroundColor(color)
    } else {
        statusBarColor = color
    }
}

@ColorInt
fun computeStatusBarColor(@ColorInt color: Int): Int {
    val alphaPercent = 0.2f
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] = hsv[2] - hsv[2] * alphaPercent
    return Color.HSVToColor(Color.alpha(color), hsv)
}