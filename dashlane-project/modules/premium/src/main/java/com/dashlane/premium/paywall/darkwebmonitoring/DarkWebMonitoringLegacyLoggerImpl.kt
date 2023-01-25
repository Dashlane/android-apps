package com.dashlane.premium.paywall.darkwebmonitoring

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode129
import com.dashlane.useractivity.log.usage.UsageLogRepository

class DarkWebMonitoringLegacyLoggerImpl(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sessionManager: SessionManager
) : DarkWebMonitoringLegacyLogger {
    override fun onShowPremiumPrompt() {
        log129(UsageLogCode129.Type.PREMIUM_PROMPT, action = UsageLogCode129.Action.DISPLAY)
    }

    override fun onClickPremiumPromptGoPremium() {
        log129(UsageLogCode129.Type.PREMIUM_PROMPT, action = UsageLogCode129.Action.GO_PREMIUM)
    }

    override fun getClickPremiumPromptClose() {
        log129(UsageLogCode129.Type.PREMIUM_PROMPT, action = UsageLogCode129.Action.CLOSE)
    }

    private fun log129(
        type: UsageLogCode129.Type,
        typeSub: String? = null,
        action: UsageLogCode129.Action? = null,
        actionSub: String? = null
    ) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(
            UsageLogCode129(
                type = type,
                typeSub = typeSub,
                action = action,
                actionSub = actionSub

            )
        )
    }
}