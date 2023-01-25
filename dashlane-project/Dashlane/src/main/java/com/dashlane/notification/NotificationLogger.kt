package com.dashlane.notification

import android.content.Intent
import androidx.annotation.StringDef



interface NotificationLogger {
    companion object {
        const val RECEIVE = "receive"
        const val DISPLAY = "display"
        const val CLICK = "click"
    }

    enum class NotificationType(val typeName: String) {
        AUTO_FILL_REMINDER("autofillReminderNotification"),
        DARK_WEB_SETUP_COMPLETE_ALERT("darkwebAlertNotificationType1"),
        DARK_WEB_NEW_ALERT("darkwebAlertNotificationType2"),
        PUBLIC_BREACH_ALERT("securityAlertNotification"),
        WHATS_APP("whatsappNotification")
    }

    @StringDef(
        DropReason.DROP_NOT_LOGGED_IN,
        DropReason.DROP_NOT_IMPACTED
    )
    annotation class LogDropReason

    object DropReason {
        const val DROP_NOT_LOGGED_IN = "drop_not_logged_in"
        const val DROP_NOT_IMPACTED = "drop_not_impacted"
        const val DROP_NOT_MATCH_PASSWORD_COUNT = "drop_not_match_password_count"
    }

    

    fun logDisplay(name: String)

    

    fun logReceive(name: String)

    

    fun logOpenIfNeeded(intent: Intent)

    

    fun logDrop(name: String, reason: String)
}