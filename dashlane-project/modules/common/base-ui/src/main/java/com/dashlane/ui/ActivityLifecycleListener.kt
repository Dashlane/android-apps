package com.dashlane.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.dashlane.ui.activities.DashlaneActivity

interface ActivityLifecycleListener : Application.ActivityLifecycleCallbacks {

    fun onActivityUserInteraction(activity: DashlaneActivity)

    fun onActivityResult(activity: DashlaneActivity, requestCode: Int, resultCode: Int, data: Intent?)

    fun onFirstActivityCreated()

    fun onFirstActivityStarted()

    fun onFirstActivityResumed()

    fun onLastActivityPaused()

    fun onLastActivityStopped()

    fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?)

    fun register(application: Application)

    fun unregister(application: Application)
}