package com.dashlane.notification

import android.content.Intent

const val EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH = "extra_breach_notification_force_refresh"

fun Intent.appendBreachNotificationExtra() {
    putExtra(EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH, true)
}
