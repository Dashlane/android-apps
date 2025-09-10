package com.dashlane.ui.activities

import android.app.Activity
import android.content.Intent
import com.dashlane.createaccount.CreateAccountComposeActivity
import com.dashlane.login.TrackingIdProvider
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.navigation.NavigationConstants
import com.dashlane.navigation.NavigationHelper
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainImmediateCoroutineDispatcher
import com.dashlane.welcome.WelcomeActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class SplashScreenIntentFactory @Inject constructor(
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val preferencesManager: PreferencesManager,
    private val sessionManager: SessionManager,
    private val loginIntentFactory: LoginIntentFactory,
    @ApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @MainImmediateCoroutineDispatcher private val mainImmediateDispatcher: CoroutineDispatcher,
) {
    fun createIntent(activity: Activity): Intent {
        val currentIntent = activity.intent
        val userPreferencesManager = preferencesManager[sessionManager.session?.username]
        val session = if (sessionManager.session != null &&
            (userPreferencesManager.isOnLoginPaywall || userPreferencesManager.isMpResetRecoveryStarted)
        ) {
            
            
            

            
            userPreferencesManager.isOnLoginPaywall = false

            
            userPreferencesManager.isMpResetRecoveryStarted = false

            sessionManager.session?.let {
                applicationCoroutineScope.launch(mainImmediateDispatcher) {
                    sessionManager.destroySession(it, byUser = false, forceLogout = false)
                }
            }
            null
        } else {
            sessionManager.session
        }

        return if (globalPreferencesManager.getDefaultUsername() != null || isTokenIntent(currentIntent)) {
            if (session != null) {
                loginIntentFactory.createApplicationHomeIntent(activity)
            } else {
                loginIntentFactory.createLoginActivityIntent(activity)
            }
        } else {
            createWelcomeIntent(activity, currentIntent)
        }
    }

    private fun createWelcomeIntent(activity: Activity, currentIntent: Intent): Intent {
        val loginIntent = loginIntentFactory.createLoginActivityIntent(activity)
            .putExtra(TrackingIdProvider.TRACKING_ID, UUID.randomUUID().toString())

        val createAccountIntent = Intent(activity, CreateAccountComposeActivity::class.java).putExtra(
            NavigationConstants.STARTED_WITH_INTENT,
            currentIntent.getParcelableExtraCompat<Intent>(NavigationConstants.STARTED_WITH_INTENT),
        )

        return WelcomeActivity.newIntent(activity, loginIntent, createAccountIntent)
    }

    private fun isTokenIntent(intent: Intent): Boolean {
        val data = intent.data
        return data != null &&
            (data.scheme == NavigationHelper.Destination.SCHEME || data.host == "universal.dashlane.com") &&
            data.lastPathSegment == NavigationHelper.Destination.MainPath.LOGIN
    }
}