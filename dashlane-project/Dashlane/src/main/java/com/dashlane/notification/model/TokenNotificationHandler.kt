package com.dashlane.notification.model

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dashlane.R
import com.dashlane.navigation.NavigationConstants
import com.dashlane.network.BaseNetworkResponse
import com.dashlane.network.webservices.authentication.GetTokenService
import com.dashlane.notification.FcmMessage
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.DashlaneIntent
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.SplashScreenActivity
import com.dashlane.util.Constants
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.notification.DashlaneNotificationBuilder
import com.dashlane.util.notification.NotificationHelper
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Clock
import java.time.Instant

class TokenNotificationHandler(
    context: Context,
    message: FcmMessage,
    private val tokenJsonProvider: TokenJsonProvider,
    private val sessionManager: SessionManager,
    private val preferencesManager: UserPreferencesManager,
    private val legacyTokenService: GetTokenService,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val clock: Clock
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

    private val tokenFromServerIfUserLoggedIn: Unit
        get() {
            val session = sessionManager.session
                ?: return 

            val username = session.userId
            if (username != recipientEmail) {
                return
            }

            legacyTokenService.createCall(username, session.uki).enqueue(
                object : Callback<BaseNetworkResponse<GetTokenService.Content>> {
                    override fun onResponse(
                        call: Call<BaseNetworkResponse<GetTokenService.Content>>,
                        response: Response<BaseNetworkResponse<GetTokenService.Content>>
                    ) {
                        if (response.isSuccessful) {
                            try {
                                preferencesManager.putBoolean(ConstantsPrefs.TOKEN_RETRIEVED_ON_PUSH, true)
                                tokenJsonProvider.json = JSONObject().apply {
                                    put("ttl", ttl)
                                    put(JSON_KEY_TOKEN, getToken(response))
                                }.toString()
                            } catch (e: JSONException) {
                                preferencesManager.remove(ConstantsPrefs.TOKEN_RETRIEVED_ON_PUSH)
                            }
                        } else {
                            preferencesManager.remove(ConstantsPrefs.TOKEN_RETRIEVED_ON_PUSH)
                        }
                    }

                    override fun onFailure(
                        call: Call<BaseNetworkResponse<GetTokenService.Content>>,
                        t: Throwable
                    ) = Unit 
                }
            )
        }

    var token: String? = null
        private set

    val lastTokenForCurrentUser: String?
        get() {
            val decrypted = tokenJsonProvider.json
            if (decrypted.isNotSemanticallyNull() && decrypted != null) {
                try {
                    val json = JSONObject(decrypted)
                    val ttl = json.getLong("ttl")
                    val now = Instant.now(clock)
                    val instant = Instant.ofEpochMilli(ttl)
                    if (now.isBefore(instant)) {
                        return json.getString(JSON_KEY_TOKEN)
                    } else {
                        removeSavedToken()
                    }
                } catch (e: JSONException) {
                    warn("getLastTokenForCurrentUser exception", "", e)
                }
            }
            return null
        }

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
            .sendBroadcast(DashlaneIntent.newInstance(Constants.BROADCASTS.NEW_TOKEN_BROADCAST))
    }

    private fun getNotificationMessage(context: Context): String {
        if (hasToken()) {
            return String.format(context.getString(R.string.gcmtint_token_is), token)
        }
        if (hasRecipient() && globalPreferencesManager.isMultipleAccountLoadedOnThisDevice) {
            return String.format(context.getString(R.string.gcmtint_token_for_user), recipientEmail)
        } else if (hasRecipient() && !globalPreferencesManager.isMultipleAccountLoadedOnThisDevice) {
            return context.getString(R.string.gcmtint_token_for_unique_user)
        }
        return context.getString(R.string.gcmtint)
    }

    private fun hasAlreadyHandled(): Boolean {
        val pushToken = Constants.GCM.Token[recipientEmail]
        return pushToken != null && ttl == pushToken.ttl
    }

    private fun hasToken(): Boolean {
        return token.isNotSemanticallyNull()
    }

    private fun notifyUser(context: Context) {
        Constants.GCM.Token[recipientEmail] = this
        Constants.GCM.TokenShouldNotify[recipientEmail] = true
        if (needWebserviceCall()) {
            tokenFromServerIfUserLoggedIn
        }
        notifyUserWithNotification(context)
        notifyUserWithPopup(context)
    }

    private fun notifyUserWithNotification(context: Context) {
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

    private fun getToken(response: Response<BaseNetworkResponse<GetTokenService.Content>>): String =
        response.body()?.content?.token ?: ""

    fun shouldNotify(username: String): Boolean {
        return hasRecipient() && recipientEmail == username && isAlive
    }

    fun needWebserviceCall(): Boolean {
        return !hasToken()
    }

    fun setShown() {
        removeSavedToken()
    }

    fun removeSavedToken() {
        tokenJsonProvider.json = null
    }

    companion object {
        private val NOTIFICATION_ID = TokenNotificationHandler::class.java.hashCode()
        private const val JSON_KEY_TOKEN = "token"
    }
}
