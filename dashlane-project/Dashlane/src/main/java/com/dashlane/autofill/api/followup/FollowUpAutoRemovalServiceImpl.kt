package com.dashlane.autofill.api.followup

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dashlane.followupnotification.services.FollowUpAutoRemovalService
import com.dashlane.logger.Log
import com.dashlane.logger.v
import com.dashlane.util.isSemanticallyNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider



class FollowUpAutoRemovalServiceImpl @Inject constructor(
    private val workManagerProvider: Provider<WorkManager>,
    @Named("autoRemovalElapsedTime")
    val notificationAutoRemovalTime: Long
) : FollowUpAutoRemovalService {
    companion object {
        private const val FOLLOW_UP_NOTIFICATION_WORKER_TAG = "follow_up_notification_worker_tag:"
    }
    private val workManager: WorkManager
        get() = workManagerProvider.get()

    override fun registerToRemove(followUpNotificationId: String) {
        if (followUpNotificationId.isSemanticallyNull()) {
            return
        }

        val workManager = this.workManager

        
        workManager.cancelAllWorkByTag(followUpNotificationId.asWorkerTag())

        try {
            val data = Data.Builder()
                .putString(RemoveFollowUpNotificationWorker.FOLLOW_UP_NOTIFICATION_ID_INPUT_KEY, followUpNotificationId)
                .build()
            val request = OneTimeWorkRequestBuilder<RemoveFollowUpNotificationWorker>()
                .setInputData(data)
                .addTag(followUpNotificationId.asWorkerTag())
                .setInitialDelay(
                    notificationAutoRemovalTime,
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueue(request)
        } catch (e: Exception) {
            Log.v(e)
        }
    }

    override fun cancelRemoval(followUpNotificationId: String) {
        if (followUpNotificationId.isSemanticallyNull()) {
            return
        }

        val workManager = this.workManager
        workManager.cancelAllWorkByTag(followUpNotificationId.asWorkerTag())
    }

    private fun String.asWorkerTag(): String {
        return FOLLOW_UP_NOTIFICATION_WORKER_TAG + this
    }
}
