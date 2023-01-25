package com.dashlane.breach

import android.app.Activity
import android.os.Bundle
import com.dashlane.notification.EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.HomeActivity
import javax.inject.Inject



class BreachManagerActivityListener @Inject constructor(private val breachManager: BreachManager) :
    AbstractActivityLifecycleListener() {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        if (activity is HomeActivity &&
            activity.intent.getBooleanExtra(EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH, false)
        ) {
            breachManager.refreshIfNecessary(true)
        }
    }
}