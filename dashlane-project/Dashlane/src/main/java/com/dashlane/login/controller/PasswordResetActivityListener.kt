package com.dashlane.login.controller

import android.app.Activity
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dashlane.async.BroadcastConstants
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.navigation.Navigator
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionTrasher
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import javax.inject.Inject

class PasswordResetActivityListener @Inject constructor(
    private val sessionManager: SessionManager,
    private val sessionTrasher: SessionTrasher,
    private val syncBroadcastManager: SyncBroadcastManager,
    private val navigator: Navigator
) : AbstractActivityLifecycleListener() {

    private var passResetReceiver: PasswordResetReceiver? = null

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if (sessionManager.session == null) return
        if (activity is DashlaneActivity) {
            passResetReceiver =
                PasswordResetReceiver(activity, sessionTrasher, syncBroadcastManager, sessionManager, navigator).apply {
                    LocalBroadcastManager.getInstance(activity)
                        .registerReceiver(this, IntentFilter(BroadcastConstants.PASSWORD_SUCCESS_BROADCAST))

                    
                    syncBroadcastManager.popPasswordBroadcast()?.let {
                        onReceive(activity, it)
                    }
                }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        passResetReceiver?.let { runCatching { LocalBroadcastManager.getInstance(activity).unregisterReceiver(it) } }
        passResetReceiver = null
    }
}