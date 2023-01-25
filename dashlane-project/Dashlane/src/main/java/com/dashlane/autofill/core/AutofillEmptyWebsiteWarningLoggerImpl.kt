package com.dashlane.autofill.core

import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningLogger
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.UserActivityLog
import com.dashlane.useractivity.log.usage.UsageLogCode33
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class AutofillEmptyWebsiteWarningLoggerImpl @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : EmptyWebsiteWarningLogger,
    AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun logDisplay(website: String) {
        log(
            type = Type.EMPTY_WEBSITE_WARNING.code,
            from = From.AUTOFILL_EMPTY_WEBSITE.code,
            confirm = Confirm.DISPLAY.code,
            website = website
        )
    }

    override fun logCancel(website: String) {
        log(
            type = Type.EMPTY_WEBSITE_WARNING.code,
            from = From.AUTOFILL_EMPTY_WEBSITE.code,
            confirm = Confirm.CANCEL.code,
            website = website
        )
    }

    override fun logUpdateAccount(website: String) {
        log(
            type = Type.EMPTY_WEBSITE_WARNING.code,
            from = From.AUTOFILL_EMPTY_WEBSITE.code,
            confirm = Confirm.UPDATE.code,
            website = website
        )
    }

    private fun log(type: String, from: String, confirm: String, website: String) {
        log(
            UsageLogCode33(
                type = type,
                from = from,
                confirm = confirm,
                website = website
            )
        )
    }

    companion object {

        enum class Type(override val code: String) : UserActivityLog.Enum {
            EMPTY_WEBSITE_WARNING("credentialWithEmptyWebsiteWarning")
        }

        enum class From(override val code: String) : UserActivityLog.Enum {
            AUTOFILL_EMPTY_WEBSITE("app_autofill_empty_website")
        }

        enum class Confirm(override val code: String) : UserActivityLog.Enum {
            DISPLAY("display"),
            UPDATE("updateWebsite"),
            CANCEL("noThanks")
        }
    }
}