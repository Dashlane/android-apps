package com.dashlane.masterpassword

import android.app.Activity
import com.dashlane.login.LoginIntents
import com.dashlane.session.SessionManager
import com.dashlane.util.clearTask
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ChangeMasterPasswordLogoutHelper @Inject constructor(
    private val sessionManager: SessionManager
) {
    fun logout(activity: Activity) {
        val session = sessionManager.session ?: return

        runBlocking { sessionManager.destroySession(session, byUser = false, forceLogout = false) }

        activity.startActivity(
            LoginIntents.createLoginActivityIntent(activity)
                .clearTask()
        )
    }
}