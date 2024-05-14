package com.dashlane.notification.model

import android.content.Context
import com.dashlane.sync.DataSync
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.notification.FcmMessage
import com.dashlane.session.SessionManager
import org.json.JSONException
import org.json.JSONObject

class SyncNotificationHandler(
    context: Context,
    message: FcmMessage,
    val dataSync: DataSync,
    val sessionManager: SessionManager
) :
    AbstractNotificationHandler(context, message) {
    private var deviceId: String? = null

    init {
        parseMessage()
        notificationId = NOTIFICATION_ID
    }

    override fun handlePushNotification() {
        syncIfUserLoggedIn()
    }

    override fun parseMessage() {
        super.parseMessage()
        val gcmData = fcmMessage.data ?: return
        try {
            val jsonFormattedData = JSONObject(gcmData)
            deviceId = jsonFormattedData.optString(JSON_KEY_DEVICE_ID)
        } catch (e: JSONException) {
            warn("parseMessage exception", "", e)
        }
    }

    private fun syncIfUserLoggedIn() {
        val session = sessionManager.session ?: return
        if ((deviceId == null || deviceId != session.deviceId) && recipientEmail == session.userId) {
            dataSync.sync(Trigger.PUSH)
        }
    }

    companion object {
        private val NOTIFICATION_ID = SyncNotificationHandler::class.java.hashCode()
        private const val JSON_KEY_DEVICE_ID = "deviceId"
    }
}