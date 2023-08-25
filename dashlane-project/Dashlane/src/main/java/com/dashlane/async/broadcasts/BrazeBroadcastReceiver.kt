package com.dashlane.async.broadcasts

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.TaskStackBuilder
import com.braze.Constants.BRAZE_PUSH_DEEP_LINK_KEY
import com.braze.Constants.BRAZE_PUSH_INTENT_NOTIFICATION_OPENED
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.premium.inappbilling.UsageLogCode35GoPremium.isDeepLinkToPremium
import com.dashlane.ui.premium.inappbilling.UsageLogCode35GoPremium.send
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.util.isSemanticallyNull

class BrazeBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val packageName = context.packageName
        if (!action.startsWith(packageName)) {
            return
        }
        if (action.endsWith(BRAZE_PUSH_INTENT_NOTIFICATION_OPENED)) {
            onNotificationOpened(context, intent)
        }
        SingletonProvider.getBrazeWrapper().requestImmediateDataFlush()
    }

    private fun onNotificationOpened(context: Context, intent: Intent) {
        val deepLink = getDeepLink(intent)
        if (deepLink.isSemanticallyNull()) {
            return
        }
        trackGoPremiumIfNeed(deepLink)
        val uriIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
        uriIntent.flags = (
            Intent.FLAG_ACTIVITY_NEW_TASK
            or Intent.FLAG_ACTIVITY_CLEAR_TOP
            or Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(uriIntent)
        try {
            stackBuilder.startActivities()
        } catch (e: ActivityNotFoundException) {
        }
    }

    private fun trackGoPremiumIfNeed(deepLink: String?) {
        if (isDeepLinkToPremium(deepLink)) {
            send(UsageLogConstant.PremiumAction.goPremiumFromAppBoyNotification)
        }
    }

    private fun getDeepLink(intent: Intent?): String? {
        return intent?.getStringExtra(BRAZE_PUSH_DEEP_LINK_KEY)
    }
}
