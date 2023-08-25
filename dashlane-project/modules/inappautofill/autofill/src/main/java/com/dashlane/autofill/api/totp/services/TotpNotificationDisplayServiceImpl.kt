package com.dashlane.autofill.api.totp.services

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dashlane.autofill.api.R
import com.dashlane.util.notification.DashlaneNotificationBuilder
import com.dashlane.util.notification.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TotpNotificationDisplayServiceImpl @Inject constructor(@ApplicationContext val context: Context) :
    TotpNotificationDisplayService {
    companion object {
        const val ACTIVE_TOTP_NOTIFICATION_ID = 0x081
        const val PASSIVE_TOTP_NOTIFICATION_ID = 0x091
    }

    override fun display(
        totpNotificationId: String,
        credentialName: String?,
        code: String,
        timeRemainingMilliseconds: Long
    ) {
        displayActiveNotification(
            totpNotificationId,
            getNotificationFirstDisplayTitle(credentialName),
            getNotificationFirstDisplayText(code.spaceInCenter()),
            timeRemainingMilliseconds
        )
    }

    private fun getNotificationFirstDisplayTitle(credentialName: String?): String {
        if (credentialName == null) {
            return context.getString(R.string.autofill_totp_notification_first_display_title_no_credential_name)
        }
        return context.getString(R.string.autofill_totp_notification_first_display_title, credentialName)
    }

    private fun getNotificationFirstDisplayText(code: String): String {
        return context.getString(R.string.autofill_totp_notification_first_display_text, code)
    }

    private fun String.spaceInCenter(): String {
        return StringBuilder(this)
            .insert(this.length / 2, " ")
            .toString()
    }

    override fun updateWithNewCode(
        totpNotificationId: String,
        credentialName: String?,
        code: String,
        timeRemainingMilliseconds: Long
    ) {
        displayActiveNotification(
            totpNotificationId,
            getNotificationUpdateNewCodeTitle(),
            getNotificationUpdateNewCodeText(code.spaceInCenter()),
            timeRemainingMilliseconds
        )
    }

    private fun getNotificationUpdateNewCodeTitle(): String {
        return context.getString(R.string.autofill_totp_notification_update_new_code_title)
    }

    private fun getNotificationUpdateNewCodeText(code: String): String {
        return context.getString(R.string.autofill_totp_notification_update_new_code_text, code)
    }

    override fun updateInformingCodeCopied(
        totpNotificationId: String,
        credentialName: String?,
        code: String,
        timeRemainingMilliseconds: Long
    ) {
        NotificationManagerCompat.from(context).cancel(ACTIVE_TOTP_NOTIFICATION_ID)
        displayPassiveNotification(
            totpNotificationId,
            getNotificationCodeCopiedTitle(),
            getNotificationCodeCopiedText(code.spaceInCenter()),
            timeRemainingMilliseconds
        )
    }

    private fun getNotificationCodeCopiedTitle(): String {
        return context.getString(R.string.autofill_totp_notification_code_copied_title)
    }

    private fun getNotificationCodeCopiedText(code: String): String {
        return context.getString(R.string.autofill_totp_notification_code_copied_text, code)
    }

    override fun updateWithSafeguardCode(
        totpNotificationId: String,
        credentialName: String?,
        code: String,
        timeRemainingMilliseconds: Long
    ) {
        displayPassiveNotification(
            totpNotificationId,
            getNotificationSafeguardCodeTitle(),
            getNotificationSafeguardCodeText(code.spaceInCenter()),
            timeRemainingMilliseconds
        )
    }

    private fun getNotificationSafeguardCodeTitle(): String {
        return context.getString(R.string.autofill_totp_notification_safeguard_code_title)
    }

    private fun getNotificationSafeguardCodeText(code: String): String {
        return context.getString(R.string.autofill_totp_notification_safeguard_code_text, code)
    }

    override fun dismissAll() {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(ACTIVE_TOTP_NOTIFICATION_ID)
        notificationManager.cancel(PASSIVE_TOTP_NOTIFICATION_ID)
    }

    private fun displayActiveNotification(
        totpNotificationId: String,
        notificationTitle: String,
        notificationText: CharSequence,
        timeRemainingMilliseconds: Long
    ) {
        val notification = totpNotificationBuilder(
            context,
            totpNotificationId,
            notificationTitle,
            notificationText,
            timeRemainingMilliseconds
        ) {
            setOnlyAlertOnce()
            setChannel(NotificationHelper.Channel.OTP)
        }.build()

        NotificationManagerCompat.from(context).notify(ACTIVE_TOTP_NOTIFICATION_ID, notification)
    }

    private fun displayPassiveNotification(
        totpNotificationId: String,
        notificationTitle: String,
        notificationText: String,
        timeRemainingMilliseconds: Long
    ) {
        val notification = totpNotificationBuilder(
            context,
            totpNotificationId,
            notificationTitle,
            notificationText,
            timeRemainingMilliseconds
        ) {
            setChannel(NotificationHelper.Channel.PASSIVE)
        }.build()

        NotificationManagerCompat.from(context).notify(PASSIVE_TOTP_NOTIFICATION_ID, notification)
    }

    private fun totpNotificationBuilder(
        context: Context,
        totpNotificationId: String,
        notificationTitle: String,
        notificationText: CharSequence,
        timeRemainingMilliseconds: Long,
        init: DashlaneNotificationBuilder.() -> Unit
    ): DashlaneNotificationBuilder {
        val copyAction = getCopyAction(totpNotificationId)
        val dismissPendingIntent =
            AutofillApiTotpNotificationReceiver.getDismissPendingIntent(context, totpNotificationId)
        val dismissAction = getDismissAction(dismissPendingIntent)

        val builder = DashlaneNotificationBuilder(context).apply {
            setIconDashlane()
            setLocalOnly()
            setContentTitle(notificationTitle)
            setContentText(notificationText, true)
            setWhenWithChronometer(timeRemainingMilliseconds, true)
            setDeleteIntent(dismissPendingIntent)
            addAction(copyAction)
            addAction(dismissAction)
        }
        builder.init()
        return builder
    }

    private fun getCopyAction(totpNotificationId: String): NotificationCompat.Action {
        return NotificationCompat.Action(
            R.drawable.ic_notification_action_code_copy,
            context.getString(R.string.autofill_totp_notification_copy_action),
            AutofillApiTotpNotificationReceiver.getCopyPendingIntent(context, totpNotificationId)
        )
    }

    private fun getDismissAction(dismissPendingIntent: PendingIntent): NotificationCompat.Action {
        return NotificationCompat.Action(
            R.drawable.ic_notification_action_dismiss,
            context.getString(R.string.autofill_totp_notification_dismiss_action),
            dismissPendingIntent
        )
    }
}
