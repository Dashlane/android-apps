package com.dashlane.notification

import android.app.Activity
import android.os.Bundle
import com.dashlane.session.SessionManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import javax.inject.Inject

class LocalNotificationCenterActivityListener @Inject constructor(
    private val notificationCreator: LocalNotificationCreator,
    private val sessionManager: SessionManager,
) : AbstractActivityLifecycleListener() {
    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        notificationCreator.register()
    }

    override fun onLastActivityPaused() {
        super.onLastActivityPaused()
        if (sessionManager.session != null) {
            notificationCreator.register()
        }
    }
}