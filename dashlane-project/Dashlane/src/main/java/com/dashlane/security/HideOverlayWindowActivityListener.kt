package com.dashlane.security

import android.app.Activity
import android.os.Build
import android.os.Bundle
import com.dashlane.ui.AbstractActivityLifecycleListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HideOverlayWindowActivityListener @Inject constructor() : AbstractActivityLifecycleListener() {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.window.setHideOverlayWindows(true)
        }
    }
}