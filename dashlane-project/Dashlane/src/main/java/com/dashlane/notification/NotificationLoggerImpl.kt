package com.dashlane.notification

import android.content.Intent
import com.dashlane.notification.NotificationLogger.Companion.CLICK
import com.dashlane.notification.NotificationLogger.Companion.DISPLAY
import com.dashlane.notification.NotificationLogger.Companion.RECEIVE
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode53
import javax.inject.Inject

class NotificationLoggerImpl @Inject constructor(
    private val installLogRepository: InstallLogRepository
) : NotificationLogger {

    override fun logDisplay(name: String) {
        sendLog(name, DISPLAY)
    }

    override fun logReceive(name: String) {
        sendLog(name, RECEIVE)
    }

    override fun logOpenIfNeeded(intent: Intent) {
        if (intent.getBooleanExtra(FcmHelper.INTENT_COME_FROM_NOTIFICATION, false)) {
            val code = intent.getStringExtra(FcmHelper.INTENT_NOTIFICATION_NAME)!!
            sendLog(code, CLICK)
        }
    }

    override fun logDrop(name: String, @NotificationLogger.LogDropReason reason: String) {
        sendLog(name, reason)
    }

    private fun sendLog(name: String, action: String) {
        installLogRepository.enqueue(
            InstallLogCode53(
                type = name,
                action = action
            )
        )
    }
}

fun FcmCode.getLogName(): String {
    return when (this) {
        FcmCode.DARK_WEB_SETUP_COMPLETE -> NotificationLogger.NotificationType.DARK_WEB_SETUP_COMPLETE_ALERT.typeName
        FcmCode.DARK_WEB_ALERT -> NotificationLogger.NotificationType.DARK_WEB_NEW_ALERT.typeName
        FcmCode.PUBLIC_BREACH_ALERT -> NotificationLogger.NotificationType.PUBLIC_BREACH_ALERT.typeName
        else -> name
    }
}