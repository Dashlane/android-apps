package com.dashlane.ui.screens.settings

import android.view.Window
import com.dashlane.ui.ScreenshotPolicy
import com.dashlane.ui.applyScreenshotAllowedFlag
import com.dashlane.util.WindowConfiguration
import javax.inject.Inject

class WindowConfigurationImpl @Inject constructor(private val mScreenshotPolicy: ScreenshotPolicy) :
    WindowConfiguration {

    override fun configure(window: Window) = window.applyScreenshotAllowedFlag(mScreenshotPolicy)
}
