package com.dashlane.login.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.events.BroadcastConstants
import com.dashlane.navigation.Navigator
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionTrasher
import com.dashlane.ui.activities.DashlaneActivity
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
        if (action == BroadcastConstants.PASSWORD_SUCCESS_BROADCAST &&
            !intent.getBooleanExtra(BroadcastConstants.SUCCESS_EXTRA, false)
        ) {
            sessionManager.session?.let {
                runBlocking { sessionTrasher.trash(username = it.username, deletePreferences = true) }
            }
            navigator.logoutAndCallLoginScreen(activity)
        }
        syncBroadcastManager.removePasswordBroadcastIntent()
    }
}
