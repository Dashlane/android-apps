package com.dashlane.crashreport.reporter

import android.content.Context
import com.dashlane.BuildConfig
import com.dashlane.crashreport.CrashReporterManager
import com.dashlane.util.PackageUtilities.getInstallerOrigin
import com.dashlane.util.userfeatures.UserFeaturesChecker
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.protocol.User



class SentryCrashReporter(
    context: Context,
    crashDeviceId: String,
    private val userFeaturesChecker: UserFeaturesChecker
) : CrashReporterManager.Client {

    private val isNonFatalEnabled: Boolean
        get() = userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.SENTRY_NON_FATAL)

    init {
        SentryAndroid.init(context) { options: SentryAndroidOptions ->
            options.release = "com.dashlane@${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"
            options.environment = if (BuildConfig.PLAYSTORE_BUILD) "Playstore" else "Dev"
            options.isEnableSessionTracking = true
        }
        Sentry.setExtra("installer", getInstallerOrigin(context))
        val user = User()
        user.id = crashDeviceId
        Sentry.setUser(user)
    }

    override fun logException(throwable: Throwable) {
        if (isNonFatalEnabled) {
            Sentry.captureException(throwable)
        }
    }

    override fun log(traceWithDate: String, rawTrace: String) {
        Sentry.addBreadcrumb(rawTrace)
    }
}