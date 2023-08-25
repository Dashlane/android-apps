package com.dashlane.notification.model

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.notification.FcmCode
import com.dashlane.notification.FcmHelper
import com.dashlane.notification.FcmMessage
import com.dashlane.notification.appendBreachNotificationExtra
import com.dashlane.notification.getJsonData
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.activities.SplashScreenActivity
import com.dashlane.util.clearTask
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.notification.buildNotification

class DarkWebAlertNotificationHandler(context: Context, message: FcmMessage) :
    AbstractNotificationHandler(context, message) {

    private val fcmHelper: FcmHelper by lazy { SingletonProvider.getFcmHelper() }

    init {
        parseMessage()
        notificationId = NOTIFICATION_ID
    }

    override fun handlePushNotification() {
        if (!isForLastLoggedInUser) return

        val (title, message) = when (fcmMessage.code) {
            FcmCode.DARK_WEB_SETUP_COMPLETE -> {
                if (fcmHelper.isAppForeground) {
                    return
                }
                if (fcmMessage.getJsonData(KEY_HAS_LEAKS) as? Boolean == true) {
                    getTitleAndMessageSetupComplete()
                } else {
                    return
                }
            }
            FcmCode.DARK_WEB_ALERT -> {
                val counter = fcmMessage.getJsonData(KEY_COUNTER) as? Int ?: return
                if (counter >= 1) {
                    getTitleAndMessageNewAlert(counter)
                } else {
                    return
                }
            }
            else -> return
        }
        val notificationIntent = DashlaneIntent.newInstance(context, SplashScreenActivity::class.java).apply {
            clearTask()
            appendBreachNotificationExtra()
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = buildNotification(context) {
            setContentTitle(title)
            setContentText(message, true)
            setIconDashlane()
            setContentIntent(pendingIntent)
            setChannel(NotificationHelper.Channel.SECURITY)
            setAutoCancel()
        }
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            
        }
    }

    private fun getTitleAndMessageSetupComplete(): Pair<String, String> {
        val title = context.getString(R.string.darkweb_notification_new_alert_title)
        val message = context.getString(R.string.darkweb_notification_new_alert_description)
        return title to message
    }

    private fun getTitleAndMessageNewAlert(counter: Int): Pair<String, String> {
        val title = context.getString(R.string.darkweb_notification_confirm_alert_title)
        val message = context.resources.getQuantityString(
            R.plurals.darkweb_notification_confirm_alert_description,
            counter,
            counter
        )
        return title to message
    }

    companion object {
        val NOTIFICATION_ID = DarkWebAlertNotificationHandler::class.java.hashCode()
        private const val KEY_COUNTER = "leakCount"
        private const val KEY_HAS_LEAKS = "hasLeaks"
    }
}