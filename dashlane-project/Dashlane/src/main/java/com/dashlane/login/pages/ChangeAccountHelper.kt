package com.dashlane.login.pages

import android.content.Context
import android.content.Intent
import com.dashlane.login.LoginActivity
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.util.clearTask
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ChangeAccountHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val globalPreferencesManager: GlobalPreferencesManager
) {
    suspend fun execute(email: String? = null): Intent {
        val session = sessionManager.session
        if (session != null) {
            
            sessionManager.destroySession(session, byUser = true, forceLogout = true)
        } else {
            
            globalPreferencesManager.isUserLoggedOut = true
        }

        if (email != null) {
            globalPreferencesManager.setLastLoggedInUser(email)
        }

        return Intent(context, LoginActivity::class.java).apply {
            clearTask()
            putExtra(LoginActivity.ALLOW_SKIP_EMAIL, email != null)
        }
    }
}