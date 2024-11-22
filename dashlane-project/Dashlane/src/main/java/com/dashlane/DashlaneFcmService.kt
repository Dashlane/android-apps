package com.dashlane

import android.content.Context
import com.braze.push.BrazeFirebaseMessagingService
import com.dashlane.breach.BreachManager
import com.dashlane.crashreport.CrashReporter
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.events.AppEvents
import com.dashlane.events.DarkWebSetupCompleteEvent
import com.dashlane.login.controller.LoginTokensModule
import com.dashlane.session.authorization
import com.dashlane.notification.FcmCode
import com.dashlane.notification.FcmHelper
import com.dashlane.notification.FcmMessage
import com.dashlane.notification.model.DarkWebAlertNotificationHandler
import com.dashlane.notification.model.PublicBreachAlertNotificationHandler
import com.dashlane.notification.model.SyncNotificationHandler
import com.dashlane.notification.model.TokenNotificationHandler
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.security.identitydashboard.breach.BreachLoader
import com.dashlane.session.SessionManager
import com.dashlane.sync.DataSync
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.components.SingletonComponent
import java.time.Clock

class DashlaneFcmService : FirebaseMessagingService() {

    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    interface FcmServiceEntryPoint {
        val fcmHelper: FcmHelper
        val appEvents: AppEvents
        val crashReporter: CrashReporter
        val dataSync: DataSync
        val sessionManager: SessionManager
        val breachManager: BreachManager
        val breachLoader: BreachLoader
        val preferencesManager: PreferencesManager
        val globalPreferencesManager: GlobalPreferencesManager
        val clock: Clock
        val loginTokensModule: LoginTokensModule
    }

    private val entryPoint: FcmServiceEntryPoint
        get() = EarlyEntryPoints.get(applicationContext, FcmServiceEntryPoint::class.java)

    override fun onNewToken(token: String) {
        
        runCatching {
            entryPoint.sessionManager.session?.let {
                entryPoint.fcmHelper.apply {
                    clearRegistration(it.username)
                    register(token = token, authorization = it.authorization)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
            message = "Received message: ${message.data}",
            tag = "CLOUD_MESSAGING"
        )
        if (DeveloperUtilities.isEmulator) return
        val data = message.data

        
        if (BrazeFirebaseMessagingService.handleBrazeRemoteMessage(this, message)) {
            return
        }
        val code = data["code"] ?: return

        entryPoint.crashReporter.addInformation("[FCM] onMessageReceived: $code")

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
                SyncNotificationHandler(
                    context,
                    fcmMessage,
                    entryPoint.dataSync,
                    entryPoint.sessionManager
                ).handlePushNotification()

            FcmCode.DARK_WEB_SETUP_COMPLETE -> {
                entryPoint.appEvents.post(DarkWebSetupCompleteEvent())
                DarkWebAlertNotificationHandler(
                    context,
                    fcmMessage,
                    entryPoint.fcmHelper,
                    entryPoint.globalPreferencesManager
                ).handlePushNotification()
            }

            FcmCode.TOKEN ->
                TokenNotificationHandler(
                    context,
                    fcmMessage,
                    entryPoint.sessionManager,
                    entryPoint.globalPreferencesManager,
                    entryPoint.clock,
                    entryPoint.loginTokensModule
                ).handlePushNotification()

            FcmCode.DARK_WEB_ALERT ->
                DarkWebAlertNotificationHandler(
                    context,
                    fcmMessage,
                    entryPoint.fcmHelper,
                    entryPoint.globalPreferencesManager
                ).handlePushNotification()

            FcmCode.PUBLIC_BREACH_ALERT ->
                PublicBreachAlertNotificationHandler(
                    context,
                    fcmMessage,
                    entryPoint.globalPreferencesManager,
                    entryPoint.breachManager,
                    entryPoint.breachLoader,
                    entryPoint.sessionManager
                ).handlePushNotification()
        }
    }
}
