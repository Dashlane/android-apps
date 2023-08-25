package com.dashlane.autofill.api.totp

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dashlane.autofill.api.totp.services.TotpNotificationUpdateService
import com.dashlane.util.isSemanticallyNull
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal class TotpNotificationUpdateServiceImpl @Inject constructor(@ApplicationContext val context: Context) :
    TotpNotificationUpdateService {
    companion object {
        private const val TOTP_NOTIFICATION_WORKER_TAG = "totp_notification_worker_tag:"
    }

    override fun registerNextUpdate(totpNotificationId: String, timeRemainingMilliseconds: Long) {
        if (totpNotificationId.isSemanticallyNull()) {
            return
        }

        val workManager = WorkManager.getInstance(context)

        
        workManager.cancelAllWorkByTag(totpNotificationId.asWorkerTag())

        try {
            val data = Data.Builder()
                .putString(UpdateTotpNotificationWorker.TOTP_NOTIFICATION_ID_INPUT_KEY, totpNotificationId)
                .build()
            val request = OneTimeWorkRequestBuilder<UpdateTotpNotificationWorker>()
                .setInputData(data)
                .addTag(totpNotificationId.asWorkerTag())
                .setInitialDelay(
                    timeRemainingMilliseconds,
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueue(request)
        } catch (e: Exception) {
        }
    }

    override fun cancelNextUpdate(totpNotificationId: String) {
        if (totpNotificationId.isSemanticallyNull()) {
            return
        }

        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(totpNotificationId.asWorkerTag())
    }

    private fun String.asWorkerTag(): String {
        return TOTP_NOTIFICATION_WORKER_TAG + this
    }
}
