package com.dashlane.login.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dashlane.async.BroadcastManager
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.navigation.NavigationUtils
import com.dashlane.session.SessionTrasher
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.usersupportreporter.UserSupportFileLogger
import com.dashlane.util.Constants
import com.dashlane.util.installlogs.DataLossTrackingLogger
import kotlinx.coroutines.runBlocking



class PasswordResetReceiver(
    private val activity: DashlaneActivity,
    private val userSupportFileLogger: UserSupportFileLogger,
    private val sessionTrasher: SessionTrasher,
    private val installLogRepository: InstallLogRepository
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Constants.BROADCASTS.PASSWORD_SUCCESS_BROADCAST) {
            if (!intent.getBooleanExtra(Constants.BROADCASTS.SUCCESS_EXTRA, false)) {
                DataLossTrackingLogger(installLogRepository).log(DataLossTrackingLogger.Reason.PASSWORD_RESET_BROADCAST)
                userSupportFileLogger.add("Password reset triggered")
                SingletonProvider.getSessionManager().session?.let {
                    runBlocking { sessionTrasher.trash(it.username, true) }
                }
                NavigationUtils.logoutAndCallLoginScreen(activity)
            }
        }
        BroadcastManager.removeBufferedIntentFor(BroadcastManager.Broadcasts.PasswordBroadcast)
    }
}