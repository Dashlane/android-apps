package com.dashlane.ui.activities

import android.app.Activity
import android.content.Intent
import com.dashlane.authenticator.AuthenticatorAppConnection
import com.dashlane.core.DataSync
import com.dashlane.createaccount.CreateAccountActivity
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.login.LoginIntents
import com.dashlane.login.TrackingIdProvider
import com.dashlane.navigation.NavigationConstants
import com.dashlane.navigation.NavigationHelper
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionCredentialsSaver
import com.dashlane.session.SessionManager
import com.dashlane.session.isServerKeyNotNull
import com.dashlane.useractivity.log.install.InstallLogCode69
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode2
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.getUsageLogCode2SenderFromOrigin
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.welcome.WelcomeActivity
import java.util.UUID
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch



class SplashScreenIntentFactory(
    private val activity: Activity,
    private val preferencesManager: GlobalPreferencesManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val installLogRepository: InstallLogRepository,
    private val sessionCredentialsSaver: SessionCredentialsSaver,
    private val authenticatorAppConnection: AuthenticatorAppConnection
) {

    

    @OptIn(DelicateCoroutinesApi::class)
    fun createIntent(): Intent {
        val currentIntent = activity.intent
        SingletonProvider.getFcmHelper().logOpenIfNeeded(currentIntent)
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

        return if (preferencesManager.getDefaultUsername() != null || isTokenIntent(currentIntent)) {
            if (session != null) {
                bySessionUsageLogRepository[session]
                    ?.enqueue(
                        UsageLogCode2(
                            otp = false,
                            sender = getUsageLogCode2SenderFromOrigin(currentIntent),
                            loadDuration = DataSync.getLastSyncDuration().toLong(),
                            keyboardActive = false
                        )
                    )

                if (session.appKey.isServerKeyNotNull) {
                    
                    installLogRepository.enqueue(
                        InstallLogCode69(
                            type = InstallLogCode69.Type.LOGIN,
                            subType = "totp_deactivated_for_this_device",
                            action = "hide_totp_screen",
                            subAction = "success"
                        )
                    )
                }

                sessionCredentialsSaver.saveCredentialsIfNecessary(session)
                LoginIntents.createHomeActivityIntent(activity)
            } else {
                preferencesManager.saveSkipIntro()
                LoginIntents.createLoginActivityIntent(activity)
            }
        } else {
            createWelcomeIntent(currentIntent)
        }
    }

    private fun createWelcomeIntent(currentIntent: Intent): Intent {
        val loginIntent = LoginIntents.createLoginActivityIntent(activity)
            .putExtra(NavigationConstants.STARTED_FROM_ONBOARDING, true)
            .putExtra(TrackingIdProvider.TRACKING_ID, UUID.randomUUID().toString())

        val createAccountIntent = Intent(activity, CreateAccountActivity::class.java).putExtra(
            NavigationConstants.STARTED_WITH_INTENT,
            currentIntent.getParcelableExtraCompat<Intent>(NavigationConstants.STARTED_WITH_INTENT)
        ).putExtra(
            CreateAccountActivity.EXTRA_PRE_FILLED_EMAIL,
            authenticatorAppConnection.otpLogin
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
        fun create(activity: Activity): SplashScreenIntentFactory {
            val preferencesManager = SingletonProvider.getGlobalPreferencesManager()
            val userPreferencesManager = SingletonProvider.getUserPreferencesManager()
            val sessionManager = SingletonProvider.getSessionManager()
            val bySessionUsageLogRepository = SingletonProvider.getComponent().bySessionUsageLogRepository
            val installLogRepository = SingletonProvider.getComponent().installLogRepository
            val sessionCredentialsSaver = SingletonProvider.getComponent().sessionCredentialsSaver
            val authenticatorAppConnection = SingletonProvider.getComponent().authenticatorAppConnection
            return SplashScreenIntentFactory(
                activity,
                preferencesManager,
                userPreferencesManager,
                sessionManager,
                bySessionUsageLogRepository,
                installLogRepository,
                sessionCredentialsSaver,
                authenticatorAppConnection
            )
        }
    }
}