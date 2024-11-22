package com.dashlane.ui.activities

import android.app.Activity
import android.content.Intent
import com.dashlane.createaccount.CreateAccountActivity
import com.dashlane.login.LoginIntents
import com.dashlane.login.TrackingIdProvider
import com.dashlane.navigation.NavigationConstants
import com.dashlane.navigation.NavigationHelper
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.welcome.WelcomeActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

class SplashScreenIntentFactory(
    private val activity: Activity,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
) {

    @OptIn(DelicateCoroutinesApi::class)
    fun createIntent(): Intent {
        val currentIntent = activity.intent
        val userPreferencesManager = preferencesManager[sessionManager.session?.username]
        val session = if (sessionManager.session != null &&
            (userPreferencesManager.isOnLoginPaywall || userPreferencesManager.isMpResetRecoveryStarted)
        ) {
            
            
            

            
            userPreferencesManager.isOnLoginPaywall = false

            
            userPreferencesManager.isMpResetRecoveryStarted = false

            sessionManager.session?.let {
                GlobalScope.launch(Dispatchers.Main.immediate) {
                    sessionManager.destroySession(it, byUser = false, forceLogout = false)
                }
            }
            null
        } else {
            sessionManager.session
        }

        return if (globalPreferencesManager.getDefaultUsername() != null || isTokenIntent(currentIntent)) {
            if (session != null) {
                LoginIntents.createHomeActivityIntent(activity)
            } else {
                globalPreferencesManager.saveSkipIntro()
                LoginIntents.createLoginActivityIntent(activity)
            }
        } else {
            createWelcomeIntent(currentIntent)
        }
    }

    private fun createWelcomeIntent(currentIntent: Intent): Intent {
        val loginIntent = LoginIntents.createLoginActivityIntent(activity)
            .putExtra(TrackingIdProvider.TRACKING_ID, UUID.randomUUID().toString())

        val createAccountIntent = Intent(activity, CreateAccountActivity::class.java).putExtra(
            NavigationConstants.STARTED_WITH_INTENT,
            currentIntent.getParcelableExtraCompat<Intent>(NavigationConstants.STARTED_WITH_INTENT)
        )

        return WelcomeActivity.newIntent(activity, loginIntent, createAccountIntent)
    }

    private fun isTokenIntent(intent: Intent): Boolean {
        val data = intent.data
        return data != null &&
            (data.scheme == NavigationHelper.Destination.SCHEME || data.host == "universal.dashlane.com") &&
            data.lastPathSegment == NavigationHelper.Destination.MainPath.LOGIN
    }

    companion object {
        @JvmStatic
        fun create(
            activity: Activity,
            globalPreferencesManager: GlobalPreferencesManager,
            preferencesManager: PreferencesManager,
            sessionManager: SessionManager,
            sessionCredentialsSaver: SessionCredentialsSaver,
        ): SplashScreenIntentFactory {
            return SplashScreenIntentFactory(
                activity,
                globalPreferencesManager,
                preferencesManager,
                sessionManager,
                sessionCredentialsSaver,
            )
        }
    }
}