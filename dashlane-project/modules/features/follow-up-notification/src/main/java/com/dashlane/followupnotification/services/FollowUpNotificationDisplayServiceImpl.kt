package com.dashlane.followupnotification.services

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationManagerCompat
import com.dashlane.followupnotification.R
import com.dashlane.followupnotification.domain.FollowUpNotification
import com.dashlane.followupnotification.domain.FollowUpNotificationsTypes
import com.dashlane.util.notification.DashlaneNotificationBuilder
import com.dashlane.util.notification.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

class FollowUpNotificationDisplayServiceImpl @Inject constructor(
    @ApplicationContext val context: Context,
    @Named("autoRemovalElapsedTime") val notificationAutoRemovalTime: Long,
    private val followUpNotificationDiscoveryService: FollowUpNotificationDiscoveryService
) : FollowUpNotificationDisplayService {
    companion object {
        const val ACTIVE_FOLLOW_UP_NOTIFICATION_ID = 0x037
        const val PASSIVE_FOLLOW_UP_NOTIFICATION_ID = 0x047
    }

    private var onScreenFollowUpNotificationId: String? = null

    override fun displayNotification(followUpNotification: FollowUpNotification) {
        val remoteViews = followUpNotification.toRemoteViews(context)
        val dismissPendingIntent =
            FollowUpNotificationDismissReceiver.getDismissPendingIntent(context, followUpNotification.id)

        val notification = DashlaneNotificationBuilder(context).apply {
            setSmallIconDashlane()
            setLocalOnly()
            setContentTitle(context.getString(R.string.dashlane_main_app_name))
            setContentText(context.getString(R.string.follow_up_notification_collapse_text))
            setWhenWithChronometer(notificationAutoRemovalTime, true)
            setCustomView(remoteViews)
            setOnlyAlertOnce()
            setChannel(NotificationHelper.Channel.FOLLOW_UP_NOTIFICATION)
            setDeleteIntent(dismissPendingIntent)
        }.build()
        onScreenFollowUpNotificationId = followUpNotification.id
        NotificationManagerCompat.from(context).notify(ACTIVE_FOLLOW_UP_NOTIFICATION_ID, notification)
        followUpNotificationDiscoveryService.updateLastNotificationItem(
            followUpNotification.vaultItemId,
            followUpNotification.id,
            false
        )
    }

    private fun FollowUpNotification.toRemoteViews(context: Context): RemoteViews =
        RemoteViews(context.packageName, R.layout.follow_up_notification_container).apply {
            addView(R.id.ll_item, attributeRemoteViewHeader(context, this@toRemoteViews.name, this@toRemoteViews.type))
            val followUpNotificationId = this@toRemoteViews.id
            this@toRemoteViews.fields.forEachIndexed { index, field ->
                val copyIntent =
                    FollowUpNotificationCopyReceiver.getCopyPendingIntent(context, followUpNotificationId, index)
                if (index > 0) {
                    addView(R.id.ll_item, attributeRemoteViewSeparator(context))
                }
                addView(
                    R.id.ll_item,
                    attributeRemoteViewFont(context, field.label, field.content.displayValue, copyIntent)
                )
            }
        }

    private fun attributeRemoteViewHeader(
        context: Context,
        attributeValue: String,
        notificationType: FollowUpNotificationsTypes
    ): RemoteViews {
        return RemoteViews(context.packageName, R.layout.follow_up_notification_header).apply {
            setTextViewText(R.id.tv_item_name, attributeValue)
            setImageViewBitmap(R.id.item_thumbnail, notificationType.icon)
        }
    }

    private fun attributeRemoteViewSeparator(context: Context): RemoteViews =
        RemoteViews(context.packageName, R.layout.follow_up_notification_separator)

    private fun attributeRemoteViewFont(
        context: Context,
        attributeLabel: CharSequence,
        attributeValue: String,
        copyClickIntent: PendingIntent
    ): RemoteViews =
        RemoteViews(context.packageName, R.layout.follow_up_notification_field).apply {
            setTextViewText(R.id.tv_attribute_name, attributeLabel)
            setTextViewText(R.id.tv_attribute_value, attributeValue)
            setOnClickPendingIntent(R.id.btn_attribute_action, copyClickIntent)
        }

    override fun dismiss(followUpNotificationId: String) {
        if (followUpNotificationId == onScreenFollowUpNotificationId) {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(ACTIVE_FOLLOW_UP_NOTIFICATION_ID)
            notificationManager.cancel(PASSIVE_FOLLOW_UP_NOTIFICATION_ID)
        }
    }
}