package com.dashlane.locking.controllers

import android.app.Activity
import android.os.Bundle
import com.dashlane.login.lock.LockManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.HomeActivity
import javax.inject.Inject

class LockSelfCheckActivityListener @Inject constructor(
    private val lockManager: LockManager
) : AbstractActivityLifecycleListener() {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityCreated(activity, savedInstanceState)
        if (activity !is HomeActivity) return
        lockManager.selfCheck()
    }
}