package com.dashlane.util.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat

fun getInAppLoginBubbleNotification(
    context: Context,
    title: String,
    message: String,
    action: NotificationCompat.Action?
): Notification {
    val builder = notificationBuilder(context) {
        setContentTitle(title)
        setContentText(message)
        setIconDashlane()
        setLocalOnly()
        setChannel(NotificationHelper.Channel.PASSIVE)
        setCategory(NotificationCompat.CATEGORY_STATUS)
        setOnlyAlertOnce()
    }
    action?.also { builder.addAction(it) }
    return builder.build()
}