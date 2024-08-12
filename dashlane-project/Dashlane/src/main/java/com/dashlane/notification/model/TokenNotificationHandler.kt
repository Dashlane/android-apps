package com.dashlane.notification.model

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dashlane.R
import com.dashlane.async.BroadcastConstants
import com.dashlane.login.controller.LoginTokensModule
import com.dashlane.navigation.NavigationConstants
import com.dashlane.notification.FcmMessage
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.security.DashlaneIntent
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.SplashScreenActivity
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.notification.DashlaneNotificationBuilder
import com.dashlane.util.notification.NotificationHelper
import org.json.JSONException
import org.json.JSONObject
import java.time.Clock
import java.time.Instant

class TokenNotificationHandler(
    context: Context,
    message: FcmMessage,
    private val sessionManager: SessionManager,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val clock: Clock,
    private val loginTokensModule: LoginTokensModule
) : AbstractNotificationHandler(context, message) {

    private val isAlive: Boolean
        get() {
            if (hasTTL()) {
                val now = Instant.now(clock)
                val instant = Instant.ofEpochMilli(ttl)
                return now.isBefore(instant)
            }
            return false
        }

    var token: String? = null
        private set

    init {
        parseMessage()
        notificationId = NOTIFICATION_ID
    }

    override fun handlePushNotification() {
        if (!isForLastLoggedInUser(globalPreferencesManager.getLastLoggedInUser()) || hasAlreadyHandled()) {
            return
        }
        setUpCancelAlarm(context)
        notifyUser(context)
    }

    override fun parseMessage() {
        super.parseMessage()
        val gcmData = fcmMessage.data ?: return
        try {
            val jsonFormattedData = JSONObject(gcmData)
            if (jsonFormattedData.has(JSON_KEY_TOKEN)) {
                token = jsonFormattedData.getString(JSON_KEY_TOKEN)
            }
        } catch (e: JSONException) {
            warn("parseMessage exception", "", e)
        }
    }

    private fun notifyUserWithPopup(context: Context) {
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(DashlaneIntent.newInstance(BroadcastConstants.NEW_TOKEN_BROADCAST))
    }

    private fun getNotificationMessage(context: Context): String {
        return String.format(context.getString(R.string.gcmtint_token_is), token)
    }

    private fun hasAlreadyHandled(): Boolean {
        val pushToken = loginTokensModule.tokenHashmap[recipientEmail]
        return pushToken != null && ttl == pushToken.ttl
    }

    private fun hasToken(): Boolean {
        return token.isNotSemanticallyNull()
    }

    private fun notifyUser(context: Context) {
        recipientEmail?.let {
            loginTokensModule.tokenHashmap[it] = this
        }
        notifyUserWithNotification(context)
        notifyUserWithPopup(context)
    }

    private fun notifyUserWithNotification(context: Context) {
        if (!hasToken()) {
            return
        }

        val notificationIntent = DashlaneIntent.newInstance(context, SplashScreenActivity::class.java)
        notificationIntent.putExtra(NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION, true)
        notificationIntent.putExtra(
            NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_USER,
            recipientEmail
        )
        val session = sessionManager.session
        if (session != null && session.userId == recipientEmail) {
            notificationIntent.putExtra(
                NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_ALREADY_LOGGED_IN,
                true
            )
        }
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
        val message = getNotificationMessage(context)
        val note = DashlaneNotificationBuilder(context)
            .setContentTitleDashlane()
            .setContentText(message, true)
            .setIconDashlane()
            .setContentIntent(pendingIntent)
            .setChannel(NotificationHelper.Channel.TOKEN)
            .setAutoCancel()
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, note)
        } catch (e: SecurityException) {
            
        }
    }

    fun shouldNotify(username: String): Boolean {
        return hasRecipient() && recipientEmail == username && isAlive
    }

    companion object {
        private val NOTIFICATION_ID = TokenNotificationHandler::class.java.hashCode()
        private const val JSON_KEY_TOKEN = "token"
    }
}
