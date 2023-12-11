package com.dashlane

import android.content.Context
import com.braze.push.BrazeFirebaseMessagingService
import com.dashlane.breach.BreachManager
import com.dashlane.core.DataSync
import com.dashlane.crashreport.CrashReporter
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.events.AppEvents
import com.dashlane.events.DarkWebSetupCompleteEvent
import com.dashlane.network.webservices.authentication.GetTokenService
import com.dashlane.notification.FcmCode
import com.dashlane.notification.FcmHelper
import com.dashlane.notification.FcmMessage
import com.dashlane.notification.model.DarkWebAlertNotificationHandler
import com.dashlane.notification.model.PublicBreachAlertNotificationHandler
import com.dashlane.notification.model.SyncNotificationHandler
import com.dashlane.notification.model.TokenJsonProvider
import com.dashlane.notification.model.TokenNotificationHandler
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.session.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import java.time.Clock
import javax.inject.Inject

@AndroidEntryPoint
class DashlaneFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmHelper: FcmHelper

    @Inject
    lateinit var appEvents: AppEvents

    @Inject
    lateinit var crashReporter: CrashReporter

    @Inject
    lateinit var dataSync: DataSync

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var breachManager: BreachManager

    @Inject
    lateinit var breachLoader: BreachLoader

    @Inject
    lateinit var tokenJsonProvider: TokenJsonProvider

    @Inject
    lateinit var preferencesManager: UserPreferencesManager

    @Inject
    lateinit var legacyTokenService: GetTokenService

    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var clock: Clock

    override fun onNewToken(token: String) {
        fcmHelper.apply {
            clearRegistration()
            register(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (DeveloperUtilities.isEmulator) return
        val data = message.data

        
        if (BrazeFirebaseMessagingService.handleBrazeRemoteMessage(this, message)) {
            return
        }
        val code = data["code"] ?: return

        crashReporter.addInformation("[FCM] onMessageReceived: $code")

        val fcmCode = FcmCode.get(code) ?: return

        
        val fcmMessage = FcmMessage(
            data = data["data"],
            login = data["login"],
            from = data["from"],
            code = fcmCode,
            message = data["message"]
        )
        processMessage(applicationContext, fcmMessage)
    }

    private fun processMessage(context: Context, fcmMessage: FcmMessage) {
        when (fcmMessage.code) {
            FcmCode.SYNC ->
                SyncNotificationHandler(context, fcmMessage, dataSync, sessionManager).handlePushNotification()

            FcmCode.DARK_WEB_SETUP_COMPLETE -> {
                appEvents.post(DarkWebSetupCompleteEvent())
                DarkWebAlertNotificationHandler(
                    context,
                    fcmMessage,
                    fcmHelper,
                    globalPreferencesManager
                ).handlePushNotification()
            }

            FcmCode.TOKEN ->
                TokenNotificationHandler(
                    context,
                    fcmMessage,
                    tokenJsonProvider,
                    sessionManager,
                    preferencesManager,
                    legacyTokenService,
                    globalPreferencesManager,
                    clock
                ).handlePushNotification()

            FcmCode.DARK_WEB_ALERT ->
                DarkWebAlertNotificationHandler(
                    context,
                    fcmMessage,
                    fcmHelper,
                    globalPreferencesManager
                ).handlePushNotification()

            FcmCode.PUBLIC_BREACH_ALERT ->
                PublicBreachAlertNotificationHandler(
                    context,
                    fcmMessage,
                    globalPreferencesManager,
                    breachManager,
                    breachLoader,
                    sessionManager
                ).handlePushNotification()
        }
    }
}
