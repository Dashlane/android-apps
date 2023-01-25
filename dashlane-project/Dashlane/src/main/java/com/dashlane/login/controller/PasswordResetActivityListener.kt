package com.dashlane.login.controller

import android.app.Activity
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dashlane.async.BroadcastManager
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionTrasher
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.Constants
import javax.inject.Inject



class PasswordResetActivityListener @Inject constructor(
    private val sessionManager: SessionManager,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val sessionTrasher: SessionTrasher,
    private val installLogRepository: InstallLogRepository
) : AbstractActivityLifecycleListener() {

    private var passResetReceiver: PasswordResetReceiver? = null

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if (sessionManager.session == null) return
        if (activity is DashlaneActivity) {
            passResetReceiver =
                PasswordResetReceiver(activity, userSupportFileLogger, sessionTrasher, installLogRepository).apply {
                    LocalBroadcastManager.getInstance(activity)
                        .registerReceiver(this, IntentFilter(Constants.BROADCASTS.PASSWORD_SUCCESS_BROADCAST))

                    
                    BroadcastManager.getLastBroadcast(BroadcastManager.Broadcasts.PasswordBroadcast)?.let {
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