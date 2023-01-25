package com.dashlane.notification

import android.content.Intent
import com.dashlane.notification.FcmHelper.Companion.INTENT_COME_FROM_NOTIFICATION
import com.dashlane.notification.FcmHelper.Companion.INTENT_NOTIFICATION_NAME

const val EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH = "extra_breach_notification_force_refresh"



fun Intent.appendNotificationExtras(name: String) {
    putExtra(INTENT_COME_FROM_NOTIFICATION, true)
    putExtra(INTENT_NOTIFICATION_NAME, name)
}



fun Intent.appendBreachNotificationExtra() {
    putExtra(EXTRA_BREACH_NOTIFICATION_FORCE_REFRESH, true)
}
