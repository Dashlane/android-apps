package com.dashlane.async.broadcasts

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.TaskStackBuilder
import com.braze.Constants.BRAZE_PUSH_DEEP_LINK_KEY
import com.braze.Constants.BRAZE_PUSH_INTENT_NOTIFICATION_OPENED
import com.dashlane.braze.BrazeWrapper
import com.dashlane.util.isSemanticallyNull
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BrazeBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var brazeWrapper: BrazeWrapper

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val packageName = context.packageName
        if (!action.startsWith(packageName)) {
            return
        }
        if (action.endsWith(BRAZE_PUSH_INTENT_NOTIFICATION_OPENED)) {
            onNotificationOpened(context, intent)
        }
        brazeWrapper.requestImmediateDataFlush()
    }

    private fun onNotificationOpened(context: Context, intent: Intent) {
        val deepLink = getDeepLink(intent)
        if (deepLink.isSemanticallyNull()) {
            return
        }
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

    private fun getDeepLink(intent: Intent?): String? {
        return intent?.getStringExtra(BRAZE_PUSH_DEEP_LINK_KEY)
    }
}
