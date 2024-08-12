package com.dashlane.ui

import android.view.Window
import android.view.WindowManager

interface ScreenshotPolicy {
    fun setScreenshotAllowed(enable: Boolean)
    fun areScreenshotAllowed(): Boolean
}

fun Window.applyScreenshotAllowedFlag(screenshotPolicy: ScreenshotPolicy) {
    if (screenshotPolicy.areScreenshotAllowed()) {
        this.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        this.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}