package com.dashlane.notification.model

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.dashlane.BuildConfig
import com.dashlane.R
import com.dashlane.breach.Breach
import com.dashlane.breach.BreachManager
import com.dashlane.breach.BreachWithOriginalJson
import com.dashlane.navigation.NavigationConstants
import com.dashlane.navigation.appendBreachNotificationExtra
import com.dashlane.notification.FcmCode
import com.dashlane.notification.FcmMessage
import com.dashlane.passwordhealth.identitydashboard.breach.BreachLoader
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.security.DashlaneIntent
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.SplashScreenActivity
import com.dashlane.util.clearTask
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.notification.buildNotification
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublicBreachAlertNotificationHandler(
    context: Context,
    message: FcmMessage,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val breachManager: BreachManager,
    private val breachLoader: BreachLoader,
    private val sessionManager: SessionManager,
    private val gson: Gson = Gson(),
    private val sessionCoroutineScopeProvider: OptionalProvider<CoroutineScope>,
) : AbstractNotificationHandler(BuildConfig.APPLICATION_ID, context, message) {
    private val sessionCoroutineScope: CoroutineScope?
        get() = sessionCoroutineScopeProvider.get()

    init {
        parseMessage()
        notificationId = NOTIFICATION_ID
    }

    override fun handlePushNotification() {
        if (!isForLastLoggedInUser(globalPreferencesManager.getLastLoggedInUser())) return

        
        if (sessionManager.session == null) {
            return
        }

        val originalJson = fcmMessage.data ?: return

        sessionCoroutineScope?.launch {
            val count = processBreachesData(originalJson)
            if (count > 0) {
                createNotification(context, count)
            }
        }
    }

    private suspend fun processBreachesData(json: String): Int =
        withContext(Dispatchers.Default) {
            val breaches = json.toBreachWithOriginalJson()
            val toSave = breachManager.getSecurityBreachesToSave(breaches)
            val breachWrapper = breachLoader.getBreachesWrapper(toSave, ignoreUserLock = true)
            breachWrapper.count()
        }

    private fun String.toBreachWithOriginalJson(): List<BreachWithOriginalJson> {
        val payload: Payload = gson.fromJson<Payload>(this, Payload::class.java)
        return payload.breaches
            ?.map { BreachWithOriginalJson(it, this) }
            ?: listOf()
    }

    private fun createNotification(context: Context, count: Int) {
        val notificationIntent =
            DashlaneIntent.newInstance(context, SplashScreenActivity::class.java).apply {
                putExtra(NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION, true)
                appendBreachNotificationExtra()
                clearTask()
            }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = context.getString(R.string.notification_public_breach_title)

        val message = context.resources.getQuantityString(
            R.plurals.notification_public_breach_description,
            count,
            count,
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

    private data class Payload(
        @SerializedName("breaches")
        val breaches: List<Breach>?,
    )

    companion object {
        val NOTIFICATION_ID = PublicBreachAlertNotificationHandler::class.java.hashCode()
    }
}