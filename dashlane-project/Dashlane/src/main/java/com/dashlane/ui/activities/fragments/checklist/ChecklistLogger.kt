package com.dashlane.ui.activities.fragments.checklist

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class ChecklistLogger @Inject constructor(
    val sessionManager: SessionManager,
    val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : ChecklistLoggerContract {

    override fun logDisplay(dismissable: Boolean, hasDarkWeb: Boolean) {
        val action = if (hasDarkWeb) "${DISPLAY}_with_dwm" else DISPLAY
        log(action, if (dismissable) "dismissable" else "undismissable")
    }

    override fun logClickAddAccount() = log(CLICK, "add_account")

    override fun logClickActivateAutofill() = log(CLICK, "activate_autofill")

    override fun logClickAddComputer() = log(CLICK, "add_computer")

    override fun logClickDarkWebMonitoring() = log(CLICK, "check_dark_web_alerts")

    override fun logClickDismiss() = log(CLICK, "dismiss")

    private fun log(action: String, subaction: String) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.let {
                UsageLogCode75(
                    type = TYPE,
                    subtype = SUBTYPE,
                    action = action,
                    subaction = subaction
                )
            }
    }

    companion object {
        private const val TYPE = "dashboard"
        private const val SUBTYPE = "welcome_checklist"
        private const val CLICK = "click"
        private const val DISPLAY = "display"
    }
}