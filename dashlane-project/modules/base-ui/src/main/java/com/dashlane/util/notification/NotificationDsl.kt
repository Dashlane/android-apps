package com.dashlane.util.notification

import android.app.Notification
import android.content.Context



fun notificationBuilder(context: Context, init: DashlaneNotificationBuilder.() -> Unit):
        DashlaneNotificationBuilder {
    val builder = DashlaneNotificationBuilder(context)
    builder.init()
    return builder
}

fun buildNotification(context: Context, init: DashlaneNotificationBuilder.() -> Unit):
        Notification = notificationBuilder(context, init).build()