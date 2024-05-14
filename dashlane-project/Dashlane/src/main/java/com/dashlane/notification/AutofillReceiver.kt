package com.dashlane.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.preference.GlobalPreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutofillReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferenceManager: GlobalPreferencesManager

    @Inject
    lateinit var autoFillNotificationCreator: AutoFillNotificationCreator

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getBooleanExtra(NOTIFICATION_NOT_NOW_EXTRA, false)
        if (!action) return

        preferenceManager.incrementAutofillNotificationDismiss()

        if (preferenceManager.getAutofillNotificationDismissCount() >= AutoFillNotificationCreator.DISMISSAL_THRESHOLD) {
            autoFillNotificationCreator.cancelAutofillNotificationWorkers()
        }

        clearNotification(context)
    }

    private fun clearNotification(context: Context?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        notificationManager.cancel(AutoFillNotificationCreator.NOTIFICATION_ID)
    }

    companion object {
        const val NOTIFICATION_NOT_NOW_EXTRA = "notification_not_now"
    }
}