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
import com.dashlane.inappautofill.InAppLoginManager
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.notification.AutofillReceiver
import com.dashlane.notification.AutofillReceiver.Companion.NOTIFICATION_NOT_NOW_EXTRA
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.NOTIFICATION_ID
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.activities.SplashScreenActivity
import com.dashlane.util.clearTask
import com.dashlane.util.notification.NotificationHelper
import com.dashlane.util.notification.buildNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutoFillNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val inAppLoginManager: InAppLoginManager,
) : Worker(context, params) {
    override fun doWork(): Result {
        val context = applicationContext
        if (!inAppLoginManager.hasAutofillApiDisabled()) return Result.success()

        val uri = NavigationUriBuilder()
            .host(NavigationHelper.Destination.MainPath.IN_APP_LOGIN)
            .build()

        val notificationIntent = DashlaneIntent.newInstance(context, SplashScreenActivity::class.java).apply {
            clearTask()
            action = Intent.ACTION_VIEW
            data = uri
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val positiveAction = NotificationCompat.Action(
            R.drawable.ic_action_item_auto_fill,
            context.getString(R.string.notification_autofill_action_positive),
            pendingIntent,
        )

        val notNowIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, AutofillReceiver::class.java).apply {
                putExtra(NOTIFICATION_NOT_NOW_EXTRA, true)
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val negativeAction = NotificationCompat.Action(
            R.drawable.close_cross,
            context.getString(R.string.notification_autofill_action),
            notNowIntent,
        )
        val notification = buildNotification(context) {
            setIconDashlane()
            setLocalOnly()
            setContentTitle(context.getString(R.string.notification_autofill_title))
            setContentText(context.getString(R.string.notification_autofill_description), true)
            setChannel(NotificationHelper.Channel.MARKETING)
            setContentIntent(pendingIntent)
            setAutoCancel()
            addAction(positiveAction)
            addAction(negativeAction)
        }
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            
        }
        return Result.success()
    }
}