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
import com.dashlane.common.logger.KibanaLogger
import com.dashlane.common.logger.usersupportlogger.UserSupportFileLogger
import com.dashlane.debug.services.DaDaDaBase
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltAndroidApp
open class DashlaneApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var dashlaneObserver: ApplicationObserver

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dadada: DaDaDaBase

    @Inject
    lateinit var kibanaLogger: KibanaLogger

    @Inject
    @ApplicationCoroutineScope
    lateinit var applicationCoroutineScope: CoroutineScope

    @Inject
    @IoCoroutineDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

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

        if (BuildConfig.DEBUG) {
        }
            UserSupportFileLogger(
                versionName = BuildConfig.VERSION_CODE.toString(),
                applicationContext = this,
                applicationScope = applicationCoroutineScope,
                ioDispatcher = ioDispatcher,
            ),
        )

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