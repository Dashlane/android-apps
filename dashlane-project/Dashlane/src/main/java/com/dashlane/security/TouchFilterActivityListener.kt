package com.dashlane.security

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.dashlane.ui.AbstractActivityLifecycleListener
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class TouchFilterActivityListener @Inject constructor() : AbstractActivityLifecycleListener() {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        activity.findViewById<View>(android.R.id.content)?.filterTouchesWhenObscured = true
    }
}