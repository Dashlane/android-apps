package com.dashlane.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.PreferencesManager
import com.dashlane.server.api.Authorization
import com.dashlane.ui.ApplicationForegroundChecker
import com.dashlane.user.Username
import com.dashlane.util.isValueNull
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class FcmHelper @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val preferencesManager: PreferencesManager,
    private val register: PushNotificationRegister,
    private val applicationForegroundChecker: ApplicationForegroundChecker,
) {

    companion object {
        
        private const val SERVER_REGISTERED = "dservNot"
    }

    val isAppForeground: Boolean
        get() = applicationForegroundChecker.isAppForeground

    fun register(authorization: Authorization.User) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener

            task.result?.let { token ->
                register(token = token, authorization = authorization)
            }
        }
    }

    fun register(token: String, authorization: Authorization.User) {
        if (token.isValueNull()) return

        val isSameToken = globalPreferencesManager.fcmRegistrationId == token
        val preferences = preferencesManager[authorization.login]

        
        
        
        if (isSameToken && preferences.getBoolean(SERVER_REGISTERED)) {
            return
        }

        
        globalPreferencesManager.fcmRegistrationId = token
        preferences.putBoolean(SERVER_REGISTERED, false)

        
        applicationCoroutineScope.launch {
            
            val success = register.register(registeredId = token, authorization = authorization)
            if (success) {
                
                preferences.putBoolean(SERVER_REGISTERED, true)
            }
        }
    }

    fun unregister(authorization: Authorization.User) {
        val registeredId = globalPreferencesManager.fcmRegistrationId ?: return
        
        applicationCoroutineScope.launch {
            val success = register.unregister(registeredId = registeredId, authorization = authorization)
            if (success) {
                
                preferencesManager[authorization.login].putBoolean(SERVER_REGISTERED, false)
            }
        }
    }

    fun clearRegistration(username: Username) {
        globalPreferencesManager.fcmRegistrationId = ""
        preferencesManager[username].putBoolean(SERVER_REGISTERED, false)
    }

    fun clearAllNotification(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun clearNotification(context: Context, id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }
}
