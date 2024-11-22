package com.dashlane.core

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dashlane.R
import com.dashlane.async.SyncBroadcastManager
import com.dashlane.debug.services.DaDaDaBase
import com.dashlane.util.notification.DashlaneNotificationBuilder
import com.dashlane.util.notification.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataSyncNotification @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val dadada: DaDaDaBase,
    private val syncBroadcastManager: SyncBroadcastManager
) {
    private val systemIsInDebug: Boolean
        get() = dadada.isEnabled
    private val notificationManager: NotificationManagerCompat
        get() = NotificationManagerCompat.from(context)

    fun showSyncNotification() {
        val icon = android.R.drawable.stat_notify_sync
        val tickerText: CharSequence = context.getString(R.string.dashlane_sync_in_progress)
        val bodyText: CharSequence =
            context.getString(R.string.your_dashlane_account_is_beeing_synchronized)
        val syncNotification = DashlaneNotificationBuilder(context)
            .setContentTitle(tickerText)
            .setContentText(bodyText)
            .setSmallIcon(icon)
            .setChannel(NotificationHelper.Channel.PASSIVE)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build() 

        try {
            if (systemIsInDebug) {
                try {
                    notificationManager.notify(SYNC_NOTIFICATION_ID, syncNotification)
                } catch (e: SecurityException) {
                    
                }
            }
        } catch (e: Exception) {
            
        }
        syncBroadcastManager.sendSyncShowProgressBroadcast(true)
    }

    fun hideSyncNotification() {
        if (systemIsInDebug) {
            notificationManager.cancel(SYNC_NOTIFICATION_ID)
        }
        syncBroadcastManager.sendSyncShowProgressBroadcast(false)
    }

    companion object {
        private const val SYNC_NOTIFICATION_ID = 0
    }
}
