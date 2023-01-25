@file:JvmName("ActivityUtils")

package com.dashlane.util

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowManager



fun Activity.hideSoftKeyboard() {
    val view = currentFocus
    if (view != null) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
}

fun Activity.getWindowSizeWithoutStatusBar(): Rect {
    val statusBarHeightId = resources.getIdentifier(
        "status_bar_height",
        "dimen",
        "android"
    )
    val statusBarHeight = if (statusBarHeightId > 0) resources.getDimensionPixelSize(statusBarHeightId) else 0

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val size = windowManager.currentWindowMetrics.bounds
        Rect(0, 0, size.width(), size.height() - statusBarHeight)
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels - statusBarHeight)
    }
}

fun Activity.findContentParent(): ViewGroup = findViewById(android.R.id.content)!!