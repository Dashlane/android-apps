package com.dashlane.darkweb.ui.result

import com.dashlane.useractivity.log.usage.UsageLogCode129
import com.dashlane.useractivity.log.usage.UsageLogRepository

class DarkWebSetupResultLoggerImpl(private val usageLogRepository: UsageLogRepository?) : DarkWebSetupResultLogger {
    override fun logShow() {
        log(UsageLogCode129.Action.SHOW)
    }

    override fun logClose() {
        log(UsageLogCode129.Action.CLOSE)
    }

    override fun logOpenApp() {
        log(UsageLogCode129.Action.CLICK)
    }

    private fun log(action: UsageLogCode129.Action) {
        usageLogRepository?.enqueue(
            UsageLogCode129(
                type = UsageLogCode129.Type.DARK_WEB_REGISTRATION,
                typeSub = TYPE_SUB_CONFIRMATION,
                action = action
            )
        )
    }

    companion object {
        const val TYPE_SUB_CONFIRMATION = "confirmation"
    }
}