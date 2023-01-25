package com.dashlane.limitations

import android.app.Activity
import android.os.Bundle
import com.dashlane.login.LoginIntents.createDeviceLimitConfirmation
import com.dashlane.login.LoginStrategy
import com.dashlane.login.LoginStrategy.Strategy.DEVICE_LIMIT
import com.dashlane.login.devicelimit.UnlinkDevicesActivity
import com.dashlane.session.SessionManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.util.clearTask
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class DeviceLimitActivityListener @Inject constructor(
    @GlobalCoroutineScope
    private val globalCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val sessionManager: SessionManager,
    private val loginStrategy: LoginStrategy
) : AbstractActivityLifecycleListener() {

    var strategy: LoginStrategy.Strategy? = null
    var isFirstLogin = true

    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        if (isFirstLogin) return
        val session = sessionManager.session ?: return
        
        globalCoroutineScope.launch(mainCoroutineDispatcher) {
            strategy = loginStrategy.getStrategy(session)
            if (!activity.isFinishing) mayShowPaywall(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if (isFirstLogin) return
        mayShowPaywall(activity)
    }

    private fun mayShowPaywall(activity: Activity) {
        if (DEVICE_LIMIT != strategy || activity !is HomeActivity) return
        
        val intent = createDeviceSyncLimit(activity)
        
        strategy = null
        activity.startActivity(intent)
    }

    private fun createDeviceSyncLimit(activity: Activity) = createDeviceLimitConfirmation(activity).apply {
        putExtra(UnlinkDevicesActivity.EXTRA_DEVICES, loginStrategy.devices.toTypedArray())
        
        clearTask()
    }
}