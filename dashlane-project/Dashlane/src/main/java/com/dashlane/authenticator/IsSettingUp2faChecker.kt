package com.dashlane.authenticator

import android.app.Activity
import com.dashlane.activatetotp.DownloadAuthenticatorAppIntroActivity
import com.dashlane.ui.AbstractActivityLifecycleListener
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("UseDataClass")
@Singleton
class IsSettingUp2faChecker @Inject constructor() {
    var isSettingUp2fa: Boolean = false
        private set

    val activityLifecycleListener = object : AbstractActivityLifecycleListener() {
        override fun onActivityResumed(activity: Activity) {
            isSettingUp2fa = activity is DownloadAuthenticatorAppIntroActivity
        }
    }
}
