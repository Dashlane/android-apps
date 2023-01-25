package com.dashlane.autofill.core

import com.dashlane.autofill.announcement.KeyboardAutofillAnnouncementLogger
import com.dashlane.autofill.announcement.KeyboardAutofillAnnouncementLogger.Confirm
import com.dashlane.autofill.announcement.KeyboardAutofillAnnouncementLogger.Type
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode33
import com.dashlane.useractivity.log.usage.UsageLogRepository

class KeyboardAutofillAnnouncementLoggerImpl(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : KeyboardAutofillAnnouncementLogger,
    AutofillLegacyLogger(sessionManager, bySessionUsageLogRepository) {

    override fun logDisplayWithAutofillOn() {
        log(
            type = Type.KEYBOARD_ANNOUNCEMENT.code,
            confirm = Confirm.DISPLAY.code
        )
    }

    override fun logPositiveWithAutofillOn() {
        log(
            type = Type.KEYBOARD_ANNOUNCEMENT.code,
            confirm = Confirm.OK.code
        )
    }

    override fun logDisplayWithAutofillOff() {
        log(
            type = Type.CALL_TO_UPGRADE.code,
            confirm = Confirm.DISPLAY.code
        )
    }

    override fun logPositiveWithAutofillOff() {
        log(
            type = Type.CALL_TO_UPGRADE.code,
            confirm = Confirm.ACTIVATE_AUTOFILL.code
        )
    }

    override fun logNegativeWithAutofillOff() {
        log(
            type = Type.CALL_TO_UPGRADE.code,
            confirm = Confirm.LATER.code
        )
    }

    private fun log(type: String, confirm: String, from: String? = null) {
        log(
            UsageLogCode33(
                type = type,
                from = from,
                confirm = confirm
            )
        )
    }
}