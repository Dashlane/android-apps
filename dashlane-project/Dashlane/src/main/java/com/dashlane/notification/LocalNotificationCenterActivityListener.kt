package com.dashlane.notification

import android.app.Activity
import android.os.Bundle
import com.dashlane.ui.AbstractActivityLifecycleListener
import javax.inject.Inject



class LocalNotificationCenterActivityListener @Inject constructor(private val notificationCreator: LocalNotificationCreator) :
    AbstractActivityLifecycleListener() {
    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        notificationCreator.register()
    }
}