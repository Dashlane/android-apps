package com.dashlane.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.CHROME_NOTIFICATION_ID
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.DISMISSAL_THRESHOLD
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.NOTIFICATION_ID
import com.dashlane.notification.creator.ChromeAutofillNotificationCreator
import com.dashlane.preference.GlobalPreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AutofillReceiver : BroadcastReceiver() {
    @Inject
    lateinit var preferenceManager: GlobalPreferencesManager

    @Inject
    lateinit var autoFillNotificationCreator: AutoFillNotificationCreator

    @Inject
    lateinit var chromeAutoFillNotificationCreator: ChromeAutofillNotificationCreator

    override fun onReceive(context: Context, intent: Intent) {
        val notNowAction = intent.getBooleanExtra(NOTIFICATION_NOT_NOW_EXTRA, false)
        val fromChrome = intent.getBooleanExtra(IS_CHROME_NOTIFICATION, false)
        clearNotification(fromChrome, context)
        if (notNowAction) reschedule(fromChrome)
    }

    private fun reschedule(fromChrome: Boolean) {
        if (fromChrome) {
            preferenceManager.incrementAutofillChromeNotificationDismiss()
            if (preferenceManager.getAutofillChromeNotificationDismissCount() >= DISMISSAL_THRESHOLD) {
                chromeAutoFillNotificationCreator.cancelAutofillNotificationWorkers()
            }
        } else {
            preferenceManager.incrementAutofillNotificationDismiss()
            if (preferenceManager.getAutofillNotificationDismissCount() >= DISMISSAL_THRESHOLD) {
                autoFillNotificationCreator.cancelAutofillNotificationWorkers()
            }
        }
    }

    private fun clearNotification(fromChrome: Boolean, context: Context?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        val id = if (fromChrome) CHROME_NOTIFICATION_ID else NOTIFICATION_ID
        notificationManager.cancel(id)
    }

    companion object {
        const val NOTIFICATION_NOT_NOW_EXTRA = "notification_not_now"
        const val IS_CHROME_NOTIFICATION = "is_chrome_notification"
    }
}