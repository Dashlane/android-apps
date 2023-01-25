package com.dashlane.security.darkwebmonitoring.detail

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.events.user.DismissSecurityAlert
import com.dashlane.security.getAlertTypeForLogs
import com.dashlane.security.getItemTypesForLogs
import com.dashlane.security.identitydashboard.breach.BreachWrapper
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLog
import com.dashlane.useractivity.log.usage.UsageLogCode129
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class BreachAlertDetailLogger @Inject constructor(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val hermesLogRepository: LogRepository,
    private val sessionManager: SessionManager
) : BreachAlertDetail.Logger {

    override fun logDelete(breachWrapper: BreachWrapper) {
        logClick129("delete_alert")
        val breach = breachWrapper.publicBreach
        hermesLogRepository.queueEvent(
            DismissSecurityAlert(
                itemTypesAffected = breach.getItemTypesForLogs(),
                securityAlertType = breach.getAlertTypeForLogs(),
                securityAlertItemId = ItemId(breach.id)
            )
        )
    }

    override fun logCheckCredentials() {
        logClick129("check_credentials")
    }

    private fun logClick129(actionSub: String? = null) {
        log129(UsageLogCode129.Action.CLICK, actionSub)
    }

    private fun log129(action: UsageLogCode129.Action, actionSub: String? = null) {
        log(UsageLogCode129(UsageLogCode129.Type.IDENTITY_DASHBOARD, SUBTYPE, action, actionSub, ORIGIN))
    }

    private fun log(log: UsageLog) {
        bySessionUsageLogRepository[sessionManager.session]?.enqueue(log)
    }

    companion object {
        private const val SUBTYPE = "dark_web_monitoring"
        private const val ORIGIN = "dark_web_monitoring"
    }
}
