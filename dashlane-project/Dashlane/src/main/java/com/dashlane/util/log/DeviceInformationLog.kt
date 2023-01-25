package com.dashlane.util.log

import android.app.Application
import android.os.Build
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.install.InstallLogCode68
import kotlin.math.max
import kotlin.math.min



class DeviceInformationLog {

    fun sendIfNecessary(application: Application) {
        val preferencesManager = SingletonProvider.getGlobalPreferencesManager()
        if (preferencesManager.getBoolean(PREF_INSTALL_LOG_68_SENT)) {
            return
        }
        send(application)
        preferencesManager.putBoolean(PREF_INSTALL_LOG_68_SENT, true)
    }

    private fun send(application: Application) {
        val dm = application.resources.displayMetrics
        val x = Math.pow((dm.widthPixels / dm.xdpi).toDouble(), 2.0)
        val y = Math.pow((dm.heightPixels / dm.ydpi).toDouble(), 2.0)
        val screenInches = Math.sqrt(x + y).toFloat()

        UserActivityComponent(application).installLogRepository
            .enqueue(
                InstallLogCode68(
                    manufacturer = Build.MANUFACTURER,
                    model = Build.MODEL,
                    screenSize = screenInches,
                    screenDensity = dm.densityDpi,
                    screenMinSize = min(dm.heightPixels, dm.widthPixels),
                    screenMaxSize = max(dm.heightPixels, dm.widthPixels)
                )
            )
    }

    companion object {
        private const val PREF_INSTALL_LOG_68_SENT = "PREF_INSTALL_LOG_68_SENT"
    }
}