package com.dashlane.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.ui.ApplicationForegroundChecker
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.util.isValueNull
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmHelper @Inject constructor(
    @ApplicationCoroutineScope
    private val applicationCoroutineScope: CoroutineScope,
    private val globalPreferencesManager: GlobalPreferencesManager,
    private val userPreferencesManager: UserPreferencesManager,
    private val register: PushNotificationRegister,
    private val applicationForegroundChecker: ApplicationForegroundChecker,
) {
    companion object {
        
        private const val SERVER_REGISTERED = "dservNot"
    }

    private var serverRegistered: Boolean
        get() = userPreferencesManager.getBoolean(SERVER_REGISTERED)
        set(value) {
            userPreferencesManager.putBoolean(SERVER_REGISTERED, value)
        }

    val isAppForeground: Boolean
        get() = applicationForegroundChecker.isAppForeground

    fun register() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener

            task.result?.let { token ->
                register(token)
            }
        }
    }

    fun register(token: String) {
        if (token.isValueNull()) return

        val isSameToken = globalPreferencesManager.fcmRegistrationId == token

        
        
        
        if (isSameToken && serverRegistered) {
            return
        }

        
        globalPreferencesManager.fcmRegistrationId = token
        serverRegistered = false

        
        applicationCoroutineScope.launch {
            
            val success = register.register(token)
            if (success) {
                
                serverRegistered = true
            }
        }
    }

    fun clearRegistration() {
        globalPreferencesManager.fcmRegistrationId = ""
        serverRegistered = false
    }

    fun clearAllNotification(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun clearNotification(context: Context, id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }
}
