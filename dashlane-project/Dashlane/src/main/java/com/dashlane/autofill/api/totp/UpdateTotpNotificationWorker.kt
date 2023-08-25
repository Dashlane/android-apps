package com.dashlane.autofill.api.totp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UpdateTotpNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {
    companion object {
        const val TOTP_NOTIFICATION_ID_INPUT_KEY = "totpNotificationId"
    }
    override fun doWork(): Result {
        val autofillApiTotp = AutofillApiTotp(applicationContext)

        val totpNotificationId = this.inputData.getString(TOTP_NOTIFICATION_ID_INPUT_KEY)
            ?: return Result.success()

        if (isStopped) return Result.success()

        autofillApiTotp.updateTotpNotification(totpNotificationId)

        return Result.success()
    }
}