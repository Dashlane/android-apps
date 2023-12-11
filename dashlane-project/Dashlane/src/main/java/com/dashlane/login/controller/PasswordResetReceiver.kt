package com.dashlane.login.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.navigation.Navigator
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionTrasher
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.Constants
import kotlinx.coroutines.runBlocking

class PasswordResetReceiver(
    private val activity: DashlaneActivity,
    private val sessionTrasher: SessionTrasher,
    private val syncBroadcastManager: SyncBroadcastManager,
    private val sessionManager: SessionManager,
    private val navigator: Navigator
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Constants.BROADCASTS.PASSWORD_SUCCESS_BROADCAST) {
            if (!intent.getBooleanExtra(Constants.BROADCASTS.SUCCESS_EXTRA, false)) {
                sessionManager.session?.let {
                    runBlocking { sessionTrasher.trash(it.username, true) }
                }
                navigator.logoutAndCallLoginScreen(activity)
            }
        }
        syncBroadcastManager.removePasswordBroadcastIntent()
    }
}
