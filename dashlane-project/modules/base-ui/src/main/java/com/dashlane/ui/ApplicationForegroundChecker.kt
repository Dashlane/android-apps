package com.dashlane.ui

import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class ApplicationForegroundChecker @Inject constructor() : AbstractActivityLifecycleListener() {

    var isAppForeground: Boolean = false
        private set

    override fun onLastActivityStopped() {
        super.onLastActivityStopped()
        isAppForeground = false
    }

    override fun onFirstActivityStarted() {
        super.onFirstActivityStarted()
        isAppForeground = true
    }
}