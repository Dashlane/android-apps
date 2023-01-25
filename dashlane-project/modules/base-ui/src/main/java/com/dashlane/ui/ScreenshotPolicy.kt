package com.dashlane.ui



interface ScreenshotPolicy {
    fun setScreenshotAllowed(enable: Boolean)
    fun areScreenshotAllowed(): Boolean
}