package com.dashlane.usersupportreporter

import com.dashlane.ui.AbstractActivityLifecycleListener
import javax.inject.Inject



class UserSupportFileAutoLoggerListener @Inject constructor(
    private val userSupportFileLogger: UserSupportFileLogger
) : AbstractActivityLifecycleListener() {

    override fun onFirstActivityStarted() {
        super.onFirstActivityStarted()
        userSupportFileLogger.add("onFirstActivityStarted")
    }

    override fun onLastActivityStopped() {
        super.onLastActivityStopped()
        userSupportFileLogger.add("onLastActivityStopped")
    }
}