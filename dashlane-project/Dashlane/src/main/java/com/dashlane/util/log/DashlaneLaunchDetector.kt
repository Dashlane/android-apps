package com.dashlane.util.log

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.text.format.DateUtils
import androidx.annotation.VisibleForTesting
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.useractivity.log.install.InstallLogCode17
import com.dashlane.useractivity.log.install.InstallLogRepository



class DashlaneLaunchDetector @VisibleForTesting internal constructor(
    private val installLogRepository: InstallLogRepository
) : ActivityLifecycleCallbacks {

    @VisibleForTesting
    var lastSentLog: Long = 0

    override fun onActivityStarted(activity: Activity) {
        if (shouldSendLog()) {
            sendInstallLog()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) { 
    }

    @VisibleForTesting
    fun shouldSendLog(): Boolean {
        return System.currentTimeMillis() - lastSentLog > MIN_INTERVAL_SEND_LOG
    }

    @VisibleForTesting
    fun sendInstallLog() {
        lastSentLog = System.currentTimeMillis()
        installLogRepository.enqueue(InstallLogCode17(subStep = "34"), false)
    }

    companion object {
        
        private const val MIN_INTERVAL_SEND_LOG = 6 * DateUtils.HOUR_IN_MILLIS

        @JvmStatic
        fun listenApplication(application: Application) {
            val dashlaneLaunchDetector = DashlaneLaunchDetector(SingletonProvider.getComponent().installLogRepository)
            application.registerActivityLifecycleCallbacks(dashlaneLaunchDetector)
        }
    }
}