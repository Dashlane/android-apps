package com.dashlane.crashreport.reporter

import android.content.Context
import com.dashlane.BuildConfig
import com.dashlane.crashreport.CrashReporterManager
import com.dashlane.util.PackageUtilities.getInstallerOrigin
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User

class SentryCrashReporter(
    context: Context,
    crashDeviceId: String,
) : CrashReporterManager.Client {

    init {
        SentryAndroid.init(context) { options ->
            options.release = "com.dashlane@${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"
            options.environment = if (BuildConfig.PLAYSTORE_BUILD) "Playstore" else "Dev"
            options.isEnableAutoSessionTracking = true
            options.isEnableUserInteractionBreadcrumbs = false 
            
            
            options.dsn =
                "randomemail@provider.com/3637169"
        }
        Sentry.setExtra("installer", getInstallerOrigin(context))
        val user = User()
        user.id = crashDeviceId
        Sentry.setUser(user)
    }

    override fun logException(throwable: Throwable) = Unit

    override fun log(traceWithDate: String, rawTrace: String) {
        Sentry.addBreadcrumb(rawTrace)
    }
}