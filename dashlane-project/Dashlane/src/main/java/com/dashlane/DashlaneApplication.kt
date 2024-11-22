package com.dashlane

import android.app.ActivityManager
import android.app.Application
import android.os.Looper
import android.os.Process
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.braze.ui.inappmessage.BrazeInAppMessageManager
import com.dashlane.debug.services.DaDaDaBase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DashlaneApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var dashlaneObserver: ApplicationObserver

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dadada: DaDaDaBase

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()

    private val isMainApplicationThread: Boolean
        get() {
            val mainLooper = Looper.getMainLooper()
            return mainLooper != null && mainLooper.isCurrentThread && "com.dashlane" == myProcessName
        }

    private val myProcessName: String?
        get() {
            val pid = Process.myPid()
            val processInfoList = (this.getSystemService(ACTIVITY_SERVICE) as? ActivityManager)
                ?.runningAppProcesses
                ?: return null
            for (processInfo in processInfoList) {
                if (processInfo.pid == pid) return processInfo.processName
            }
            return null
        }

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreate() {
        super.onCreate()

        BrazeInAppMessageManager.getInstance().ensureSubscribedToInAppMessageEvents(applicationContext)
        dadada.refreshAsync(this)

        updateWebViewDataDirectory()
        if (isMainApplicationThread) {
            dashlaneObserver.onCreate(this)
        }
    }

    override fun onTerminate() {
        if (isMainApplicationThread) {
            dashlaneObserver.onTerminate(this)
        }
        super.onTerminate()
    }

    private fun updateWebViewDataDirectory() {
        val processName = getProcessName()
        if (processName == null || "com.dashlane" == processName) {
            
            return
        }
        
        
        WebView.setDataDirectorySuffix("webview-$processName")
    }
}