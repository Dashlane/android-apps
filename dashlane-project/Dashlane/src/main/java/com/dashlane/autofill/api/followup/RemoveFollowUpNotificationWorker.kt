package com.dashlane.autofill.api.followup

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dashlane.followupnotification.FollowUpNotificationComponent

class RemoveFollowUpNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    companion object {
        const val FOLLOW_UP_NOTIFICATION_ID_INPUT_KEY = "totpNotificationId"
    }

    override fun doWork(): Result {
        val followUpNotificationApi = FollowUpNotificationComponent(applicationContext)
            .followUpNotificationApiProvider
            .getFollowUpNotificationApi()

        val followUpNotificationId = this.inputData.getString(FOLLOW_UP_NOTIFICATION_ID_INPUT_KEY)
            ?: return Result.success()

        if (isStopped) return Result.success()

        followUpNotificationApi.dismissFollowUpNotifications(followUpNotificationId, true)

        return Result.success()
    }
}