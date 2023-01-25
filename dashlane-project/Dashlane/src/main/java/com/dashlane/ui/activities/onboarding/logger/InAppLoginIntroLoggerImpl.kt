package com.dashlane.ui.activities.onboarding.logger

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode131

class InAppLoginIntroLoggerImpl(private val usageLogRepository: UsageLogRepository?) : InAppLoginIntroLogger {

    override fun logShowInAppLoginScreen() {
        log(UsageLogCode131.Type.AUTOFILL_ACTIVATION_PROMPT, UsageLogCode131.Action.DISPLAY)
    }

    override fun logSkip(type: UsageLogCode131.Type) {
        log(type, UsageLogCode131.Action.SKIP)
    }

    override fun logBack(type: UsageLogCode131.Type) {
        log(type, UsageLogCode131.Action.BACK)
    }

    override fun logActivateAutofill() {
        log(UsageLogCode131.Type.AUTOFILL_ACTIVATION_PROMPT, UsageLogCode131.Action.ACTIVATE_AUTOFILL)
    }

    private fun log(type: UsageLogCode131.Type, action: UsageLogCode131.Action, actionSub: String? = null) {
        usageLogRepository?.enqueue(
            UsageLogCode131(
                type = type,
                action = action,
                actionSub = actionSub
            )
        )
    }
}