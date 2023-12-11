package com.dashlane.notification.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.SystemClock
import androidx.core.app.NotificationManagerCompat
import com.dashlane.notification.FcmMessage
import com.dashlane.security.DashlaneIntent
import com.dashlane.util.Constants
import com.dashlane.util.isNotSemanticallyNull
import org.json.JSONException
import org.json.JSONObject

abstract class AbstractNotificationHandler protected constructor(
    val context: Context,
    val fcmMessage: FcmMessage
) {

    protected var recipientEmail: String? = null
        private set

    protected var ttl: Long = -1

    protected var notificationId = 0

    fun clearNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    abstract fun handlePushNotification()

    protected fun isForLastLoggedInUser(lastLoggedInUser: String): Boolean {
        return lastLoggedInUser == recipientEmail
    }

    protected open fun parseMessage() {
        val gcmData = fcmMessage.data ?: return
        try {
            val jsonFormattedData = JSONObject(gcmData)
            recipientEmail = fcmMessage.login
            if (jsonFormattedData.has(JSON_KEY_TTL)) {
                ttl = jsonFormattedData.getLong(JSON_KEY_TTL)
            }
        } catch (e: JSONException) {
        }
    }

    protected fun hasTTL(): Boolean {
        return ttl >= 0
    }

    protected fun setUpCancelAlarm(context: Context) {
        if (hasTTL()) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i = DashlaneIntent.newInstance(Constants.GCM.CLEAR_GCM_NOTIFICATION)
            i.putExtra("notificationId", notificationId)
            val intentExecuted = PendingIntent.getBroadcast(
                context,
                0,
                i,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + ttl] = intentExecuted
        }
    }

    protected fun hasRecipient(): Boolean {
        return recipientEmail.isNotSemanticallyNull()
    }

    companion object {
        private const val JSON_KEY_TTL = "ttl"
    }
}
