package com.dashlane.util.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dashlane.ui.R
import com.dashlane.util.toBitmap



class DashlaneNotificationBuilder constructor(private val mContext: Context) {
    val notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(mContext, NotificationHelper.DEFAULT_CHANNEL)

    fun setContentTitleDashlane(): DashlaneNotificationBuilder {
        return setContentTitle(mContext.getString(R.string.dashlane_main_app_name))
    }

    fun setContentTitle(text: CharSequence): DashlaneNotificationBuilder {
        notificationBuilder.setContentTitle(text)
        return this
    }

    @JvmOverloads
    fun setContentText(text: CharSequence, bigTextStyle: Boolean = false): DashlaneNotificationBuilder {
        notificationBuilder.setContentText(text)
        if (bigTextStyle) {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(text))
        }
        return this
    }

    fun setIconDashlane(): DashlaneNotificationBuilder {
        val largeIconResId = R.drawable.ic_notification_large_icon
        val icon = ContextCompat.getDrawable(mContext, largeIconResId)!!.toBitmap()
        setLargeIcon(icon)
        return setSmallIconDashlane()
    }

    fun setSmallIconDashlane(): DashlaneNotificationBuilder {
        val smallIconResId = R.drawable.ic_notification_small_icon
        
        
        notificationBuilder.color = ContextCompat.getColor(mContext, R.color.dashlane_notification)
        return setSmallIcon(smallIconResId)
    }

    fun setSmallIcon(@DrawableRes iconResId: Int): DashlaneNotificationBuilder {
        notificationBuilder.setSmallIcon(iconResId)
        return this
    }

    fun setLargeIcon(icon: Bitmap): DashlaneNotificationBuilder {
        notificationBuilder.setLargeIcon(icon)
        return this
    }

    fun setAutoCancel(): DashlaneNotificationBuilder {
        notificationBuilder.setAutoCancel(true)
        return this
    }

    fun setContentIntent(pendingIntent: PendingIntent): DashlaneNotificationBuilder {
        notificationBuilder.setContentIntent(pendingIntent)
        return this
    }

    fun setNumber(number: Int): DashlaneNotificationBuilder {
        notificationBuilder.setNumber(number)
        return this
    }

    fun setLocalOnly(): DashlaneNotificationBuilder {
        notificationBuilder.setLocalOnly(true)
        return this
    }

    fun setOnlyAlertOnce(): DashlaneNotificationBuilder {
        notificationBuilder.setOnlyAlertOnce(true)
        return this
    }

    fun setCategory(category: String): DashlaneNotificationBuilder {
        notificationBuilder.setCategory(category)
        return this
    }

    fun setProgressPercent(percent: Int): DashlaneNotificationBuilder {
        notificationBuilder.setProgress(100, percent, false)
        return this
    }

    fun setDeleteIntent(intent: PendingIntent): DashlaneNotificationBuilder {
        notificationBuilder.setDeleteIntent(intent)
        return this
    }

    fun addAction(action: NotificationCompat.Action): DashlaneNotificationBuilder {
        notificationBuilder.addAction(action)
        return this
    }

    fun setChannel(channelInformation: NotificationHelper.Channel): DashlaneNotificationBuilder {
        notificationBuilder.setChannelId(channelInformation.id)
        notificationBuilder.priority = channelInformation.priority
        return this
    }

    fun setWhenWithChronometer(delayInMillis: Long, countdown: Boolean = false): DashlaneNotificationBuilder {
        notificationBuilder.setShowWhen(true)
        notificationBuilder.setWhen(System.currentTimeMillis() + delayInMillis)
        notificationBuilder.setUsesChronometer(true)
        notificationBuilder.setExtras(Bundle())
        notificationBuilder.setChronometerCountDown(countdown)
        return this
    }

    fun setCustomView(notificationLayoutExpanded: RemoteViews): DashlaneNotificationBuilder {
        notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        notificationBuilder.setCustomBigContentView(notificationLayoutExpanded)
        return this
    }

    fun build(): Notification {
        return notificationBuilder.build()
    }
}
