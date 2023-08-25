package com.dashlane.followupnotification.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.followupnotification.FollowUpNotificationComponent

object FollowUpNotificationReceiver {
    const val NOTIFICATION_REQUEST_CODE = 91
    const val NOTIFICATION_FOLLOW_UP_NOTIFICATION_ID_EXTRA =
        "notification_follow_up_notification_id"
    const val NOTIFICATION_COPY_FIELD_INDEX_EXTRA = "notification_copy_field_index"
}

class FollowUpNotificationDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val followUpNotificationId =
            intent.getStringExtra(FollowUpNotificationReceiver.NOTIFICATION_FOLLOW_UP_NOTIFICATION_ID_EXTRA)
                ?: return
        val followUpNotificationApi = context.getFollowUpNotificationApi()
        followUpNotificationApi.dismissFollowUpNotifications(followUpNotificationId, false)
        return
    }

    companion object {
        fun getDismissPendingIntent(
            context: Context,
            followUpNotificationId: String
        ): PendingIntent {
            val requestCode = FollowUpNotificationReceiver.NOTIFICATION_REQUEST_CODE

            return PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, FollowUpNotificationDismissReceiver::class.java).apply {
                    putExtra(
                        FollowUpNotificationReceiver.NOTIFICATION_FOLLOW_UP_NOTIFICATION_ID_EXTRA,
                        followUpNotificationId
                    )
                },
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

class FollowUpNotificationCopyReceiver : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val followUpNotificationId =
            intent.getStringExtra(FollowUpNotificationReceiver.NOTIFICATION_FOLLOW_UP_NOTIFICATION_ID_EXTRA)
        if (followUpNotificationId == null) {
            finish()
            return
        }
        val followUpNotificationApi = applicationContext.getFollowUpNotificationApi()
        val copyFieldIndex =
            intent.getIntExtra(FollowUpNotificationReceiver.NOTIFICATION_COPY_FIELD_INDEX_EXTRA, -1)
        followUpNotificationApi.copyToClipboard(followUpNotificationId, copyFieldIndex)
        finish()
    }

    companion object {
        fun getCopyPendingIntent(
            context: Context,
            followUpNotificationId: String,
            copyFieldIndex: Int
        ): PendingIntent {
            val requestCode =
                FollowUpNotificationReceiver.NOTIFICATION_REQUEST_CODE + copyFieldIndex

            return PendingIntent.getActivity(
                context,
                requestCode,
                Intent(context, FollowUpNotificationCopyReceiver::class.java).apply {
                    putExtra(
                        FollowUpNotificationReceiver.NOTIFICATION_FOLLOW_UP_NOTIFICATION_ID_EXTRA,
                        followUpNotificationId
                    )
                    putExtra(
                        FollowUpNotificationReceiver.NOTIFICATION_COPY_FIELD_INDEX_EXTRA,
                        copyFieldIndex
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                },
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

internal fun Context.getFollowUpNotificationApi() = FollowUpNotificationComponent(this)
    .followUpNotificationApiProvider
    .getFollowUpNotificationApi()