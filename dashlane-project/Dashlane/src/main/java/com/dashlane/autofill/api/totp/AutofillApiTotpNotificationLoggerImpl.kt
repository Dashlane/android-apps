package com.dashlane.autofill.api.totp

import com.dashlane.autofill.api.totp.model.TotpNotification
import com.dashlane.autofill.api.totp.services.AutofillApiTotpNotificationLogger
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.vault.model.navigationUrl
import javax.inject.Inject

class AutofillApiTotpNotificationLoggerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) :
    AutofillApiTotpNotificationLogger {

    companion object {
        private const val TOTP_NOTIFICATION_UL75_TYPE = "otpNotification"
        private const val TOTP_NOTIFICATION_UL75_SENT_ACTION = "sent"
        private const val TOTP_NOTIFICATION_UL75_COPY_ACTION = "copy"
        private const val TOTP_NOTIFICATION_UL75_DISMISS_ACTION = "dismiss"
        private const val TOTP_NOTIFICATION_UL75_COPY_SUB_ACTION_PREFIX = "copy"
        private const val TOTP_NOTIFICATION_UL75_ITERATION_SUB_TYPE_PREFIX = "iteration"
    }

    override fun totpNotificationDisplay(totpNotification: TotpNotification) {
        log(
            UsageLogCode75(
                action = TOTP_NOTIFICATION_UL75_SENT_ACTION,
                type = TOTP_NOTIFICATION_UL75_TYPE,
                subtype = totpNotification.totalCodeUpdates.asIterationSubType(),
                website = totpNotification.website
            )
        )
    }

    override fun totpNotificationCopied(totpNotification: TotpNotification) {
        log(
            UsageLogCode75(
                action = TOTP_NOTIFICATION_UL75_COPY_ACTION,
                subaction = totpNotification.totalCodeCopies.asCopySubAction(),
                type = TOTP_NOTIFICATION_UL75_TYPE,
                subtype = totpNotification.totalCodeUpdates.asIterationSubType(),
                website = totpNotification.website
            )
        )
    }

    override fun totpNotificationDismiss(totpNotification: TotpNotification?) {
        log(
            UsageLogCode75(
                action = TOTP_NOTIFICATION_UL75_DISMISS_ACTION,
                type = TOTP_NOTIFICATION_UL75_TYPE,
                subtype = totpNotification?.totalCodeUpdates?.asIterationSubType(),
                website = totpNotification?.website
            )
        )
    }

    private fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(log)
    }

    private val TotpNotification.website: String?
        get() = this.credential.navigationUrl?.toUrlOrNull()?.root

    private fun Int.asIterationSubType(): String {
        return TOTP_NOTIFICATION_UL75_ITERATION_SUB_TYPE_PREFIX + this
    }

    private fun Int.asCopySubAction(): String {
        return TOTP_NOTIFICATION_UL75_COPY_SUB_ACTION_PREFIX + this
    }
}