package com.dashlane.darkweb.ui.intro

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode129

class DarkWebSetupIntroLoggerImpl(private val usageLogRepository: UsageLogRepository?) : DarkWebSetupIntroLogger {
    override fun logShow(origin: String?) {
        log(UsageLogCode129.Action.SHOW, origin)
    }

    override fun logCancel() {
        log(UsageLogCode129.Action.CANCEL)
    }

    override fun logNext() {
        log(UsageLogCode129.Action.NEXT)
    }

    private fun log(action: UsageLogCode129.Action, origin: String? = null) {
        usageLogRepository?.enqueue(
            UsageLogCode129(
                type = UsageLogCode129.Type.DARK_WEB_REGISTRATION,
                typeSub = TYPE_SUB_LANDING_PAGE,
                action = action,
                origin = origin
            )
        )
    }

    companion object {
        const val TYPE_SUB_LANDING_PAGE = "landing_page"
    }
}