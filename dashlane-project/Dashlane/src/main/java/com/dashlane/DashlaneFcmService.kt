package com.dashlane

import android.content.Context
import com.braze.push.BrazeFirebaseMessagingService
import com.dashlane.crashreport.CrashReporter
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.events.AppEvents
import com.dashlane.events.DarkWebSetupCompleteEvent
import com.dashlane.logger.utils.LogsSender
import com.dashlane.notification.FcmCode
import com.dashlane.notification.FcmHelper
import com.dashlane.notification.FcmMessage
import com.dashlane.notification.model.DarkWebAlertNotificationHandler
import com.dashlane.notification.model.PublicBreachAlertNotificationHandler
import com.dashlane.notification.model.SyncNotificationHandler
import com.dashlane.notification.model.TokenNotificationHandler
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
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
    lateinit var logsSender: LogsSender

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
                SyncNotificationHandler(context, fcmMessage).handlePushNotification()

            FcmCode.DARK_WEB_SETUP_COMPLETE -> {
                appEvents.post(DarkWebSetupCompleteEvent())
                DarkWebAlertNotificationHandler(context, fcmMessage).handlePushNotification()
            }

            FcmCode.TOKEN ->
                TokenNotificationHandler(context, fcmMessage).handlePushNotification()

            FcmCode.USAGE_LOG -> {
                logsSender.flushLogs()
            }

            FcmCode.DARK_WEB_ALERT ->
                DarkWebAlertNotificationHandler(context, fcmMessage).handlePushNotification()

            FcmCode.PUBLIC_BREACH_ALERT ->
                PublicBreachAlertNotificationHandler(context, fcmMessage).handlePushNotification()
        }
    }
}
