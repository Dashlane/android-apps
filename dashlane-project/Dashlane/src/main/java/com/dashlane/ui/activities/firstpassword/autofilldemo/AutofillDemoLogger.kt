package com.dashlane.ui.activities.firstpassword.autofilldemo

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class AutofillDemoLogger @Inject constructor(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sessionManager: SessionManager
) : AutofillDemo.Logger {

    override fun display() {
        log(ACTION_DISPLAY, "page_autofill")
    }

    override fun autofillShown() {
        log(ACTION_DISPLAY, "autofill_preview")
    }

    override fun autofillTriggered() {
        log(ACTION_CLICK, "autofill")
    }

    override fun finishClicked() {
        log(ACTION_CLICK, "finish")
    }

    private fun log(action: String, subAction: String? = null) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode35(
                    type = "autofill_demo",
                    action = action,
                    subaction = subAction
                )
            )
    }

    companion object {
        private const val ACTION_DISPLAY = "display"
        private const val ACTION_CLICK = "click"
    }
}