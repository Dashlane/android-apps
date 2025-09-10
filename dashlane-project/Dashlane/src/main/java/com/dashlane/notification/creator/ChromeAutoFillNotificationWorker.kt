package com.dashlane.notification.creator

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dashlane.R
import com.dashlane.autofillapi.ChromeAutofillDetector
import com.dashlane.autofillapi.ChromeAutofillSettingsHelper.buildChromeAutofillSettingsIntent
import com.dashlane.inappautofill.InAppLoginManager
import com.dashlane.notification.AutofillReceiver
import com.dashlane.notification.AutofillReceiver.Companion.IS_CHROME_NOTIFICATION
import com.dashlane.notification.AutofillReceiver.Companion.NOTIFICATION_NOT_NOW_EXTRA
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.CHROME_NOTIFICATION_ID
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.notification.buildNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ChromeAutoFillNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val inAppLoginManager: InAppLoginManager,
    private val detector: ChromeAutofillDetector,
) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        val chromeAutofillStatus = detector.getChromeAutofillStatus(context.contentResolver, context.packageManager)
        val isChromeThirdPartyAutofillDisabled = detector.isThirdPartyAutofillDeactivated(chromeAutofillStatus)
        if (!inAppLoginManager.hasAutofillApiDisabled() && !isChromeThirdPartyAutofillDisabled) return Result.success()

        val autofillSettingsIntent = buildChromeAutofillSettingsIntent(chromeAutofillStatus.packageName)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            autofillSettingsIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val positiveAction = NotificationCompat.Action(
            R.drawable.ic_action_item_auto_fill,
            context.getString(R.string.notification_autofill_chrome_action_positive),
            pendingIntent,
        )

        val notNowIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, AutofillReceiver::class.java).apply {
                putExtra(NOTIFICATION_NOT_NOW_EXTRA, true)
                putExtra(IS_CHROME_NOTIFICATION, true)
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val negativeAction = NotificationCompat.Action(
            R.drawable.close_cross,
            context.getString(R.string.notification_autofill_chrome_action),
            notNowIntent,
        )
        val notification = buildNotification(context) {
            setIconDashlane()
            setLocalOnly()
            setContentTitle(context.getString(R.string.notification_autofill_chrome_title))
            setContentText(context.getString(R.string.notification_autofill_chrome_description), true)
            setChannel(NotificationHelper.Channel.MARKETING)
            setContentIntent(pendingIntent)
            setAutoCancel()
            addAction(positiveAction)
            addAction(negativeAction)
        }
        try {
            NotificationManagerCompat.from(context).notify(CHROME_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            
        }
        return Result.success()
    }
}