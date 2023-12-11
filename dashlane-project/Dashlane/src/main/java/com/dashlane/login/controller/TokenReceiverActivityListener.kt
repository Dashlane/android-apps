package com.dashlane.login.controller

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dashlane.R
import com.dashlane.navigation.NavigationConstants
import com.dashlane.network.webservices.authentication.GetTokenService
import com.dashlane.notification.model.TokenJsonProvider
import com.dashlane.notification.model.TokenNotificationHandler
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.Constants
import com.dashlane.util.Toaster
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenReceiverActivityListener @Inject constructor(
    private val sessionManager: SessionManager,
    private val lockRepository: LockRepository,
    private val toaster: Toaster,
    private val legacyTokenService: GetTokenService,
    @ApplicationContext
    private val context: Context,
    private val tokenJsonProvider: TokenJsonProvider,
    private val preferencesManager: UserPreferencesManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val clock: Clock
) : AbstractActivityLifecycleListener() {

    private var receiver: NewTokenReceiver? = null

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        receiver = NewTokenReceiver(activity)
        LocalBroadcastManager.getInstance(activity).registerReceiver(
            receiver!!,
            IntentFilter(Constants.BROADCASTS.NEW_TOKEN_BROADCAST)
        )
        checkForTokenMessage(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        receiver?.let {
            runCatching { LocalBroadcastManager.getInstance(activity).unregisterReceiver(it) }
        }
    }

    private fun checkForTokenMessage(activity: Activity) {
        if (activity !is DashlaneActivity || activity.isFinishing || activity.isChangingConfigurations) return
        val session = sessionManager.session ?: return

        val username = session.userId
        if (lockRepository.getLockManager(session).isLocked) {
            val intent = activity.intent
            if (intent != null && intent.getBooleanExtra(
                    NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION,
                    false
                )
            ) {
                val user = intent.getStringExtra(
                    NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_USER
                )
                val alreadyLoggedIn = intent.getBooleanExtra(
                    NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_ALREADY_LOGGED_IN,
                    false
                )
                if (username == user && alreadyLoggedIn) {
                    toaster.show(R.string.push_token_toast_unlock_to_get_token, Toast.LENGTH_LONG)
                }
            }
            return
        }

        val pushToken = Constants.GCM.Token[username] ?: return
        val tokenGcmShouldNotify = pushToken.shouldNotify(username)
        Constants.GCM.TokenShouldNotify[username] = tokenGcmShouldNotify

        if (tokenGcmShouldNotify) {
            val tokenNotificationHandler = TokenNotificationHandler(
                context = context,
                message = pushToken.fcmMessage,
                tokenJsonProvider = tokenJsonProvider,
                sessionManager = sessionManager,
                preferencesManager = preferencesManager,
                legacyTokenService = legacyTokenService,
                globalPreferencesManager = globalPreferencesManager,
                clock = clock
            )
            TokenChecker(legacyTokenService, tokenNotificationHandler).checkAndDisplayTokenIfNeeded(
                activity,
                username,
                session.uki
            )
        }
    }

    private inner class NewTokenReceiver(private val activity: Activity) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Constants.BROADCASTS.NEW_TOKEN_BROADCAST) {
                checkForTokenMessage(activity)
            }
        }
    }
}