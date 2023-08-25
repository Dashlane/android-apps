package com.dashlane.autofill.api.totp.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dashlane.autofill.api.totp.AutofillApiTotp
import com.dashlane.util.tryOrNull

class AutofillApiTotpNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(NOTIFICATION_ACTION_EXTRA) ?: return
        val totpNotificationId = intent.getStringExtra(NOTIFICATION_TOTP_NOTIFICATION_ID_EXTRA) ?: return
        val notificationActions = tryOrNull { NotificationActions.valueOf(action) } ?: return

        val applicationContext = context.applicationContext

        when (notificationActions) {
            NotificationActions.DISMISS -> dismiss(totpNotificationId, applicationContext)
            NotificationActions.COPY -> copy(totpNotificationId, applicationContext)
        }
    }

    private fun dismiss(totpNotificationId: String, applicationContext: Context) {
        AutofillApiTotp(applicationContext).removeTotpNotification(totpNotificationId)
    }

    private fun copy(totpNotificationId: String, applicationContext: Context) {
        AutofillApiTotp(applicationContext).clipboardTotpCode(totpNotificationId)
    }

    enum class NotificationActions {
        COPY,
        DISMISS
    }

    companion object {
        private const val NOTIFICATION_REQUEST_CODE = 81
        private const val NOTIFICATION_TOTP_NOTIFICATION_ID_EXTRA = "notification_totp_notification_id"
        private const val NOTIFICATION_ACTION_EXTRA = "notification_copy"

        fun getCopyPendingIntent(
            context: Context,
            totpNotificationId: String
        ): PendingIntent {
            return getPendingIntent(context, totpNotificationId, NotificationActions.COPY)
        }

        fun getDismissPendingIntent(
            context: Context,
            totpNotificationId: String
        ): PendingIntent {
            return getPendingIntent(context, totpNotificationId, NotificationActions.DISMISS)
        }

        fun getPendingIntent(
            context: Context,
            totpNotificationId: String,
            action: NotificationActions
        ): PendingIntent {
            val requestCode = NOTIFICATION_REQUEST_CODE + action.ordinal
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, AutofillApiTotpNotificationReceiver::class.java).apply {
                    putExtra(NOTIFICATION_TOTP_NOTIFICATION_ID_EXTRA, totpNotificationId)
                    putExtra(NOTIFICATION_ACTION_EXTRA, action.name)
                },
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}