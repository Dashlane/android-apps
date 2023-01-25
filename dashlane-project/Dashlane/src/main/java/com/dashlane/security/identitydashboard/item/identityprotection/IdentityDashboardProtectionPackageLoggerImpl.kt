package com.dashlane.security.identitydashboard.item.identityprotection

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode130
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class IdentityDashboardProtectionPackageLoggerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : IdentityDashboardProtectionPackageLogger {

    override fun logOnActivePackageShow() {
        log(UsageLogCode130.TypeSub.CREDIT_SCORE_MONITORING, UsageLogCode130.Action.SHOW)
        log(UsageLogCode130.TypeSub.IDENTITY_THEFT, UsageLogCode130.Action.SHOW)
        log(UsageLogCode130.TypeSub.IDENTITY_RESTORATION, UsageLogCode130.Action.SHOW)
    }

    override fun logOnActiveSeeCreditView() {
        log(UsageLogCode130.TypeSub.CREDIT_SCORE_MONITORING, UsageLogCode130.Action.SEE_CREDIT_VIEW)
    }

    override fun logOnActiveProtectionLearnMore() {
        log(UsageLogCode130.TypeSub.IDENTITY_THEFT, UsageLogCode130.Action.LEARN_MORE)
    }

    override fun logOnActiveRestorationLearnMore() {
        log(UsageLogCode130.TypeSub.IDENTITY_RESTORATION, UsageLogCode130.Action.LEARN_MORE)
    }

    private fun log(subType: UsageLogCode130.TypeSub, action: UsageLogCode130.Action, subAction: String? = null) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode130(
                    type = UsageLogCode130.Type.IDENTITY_DASHBOARD,
                    typeSub = subType,
                    action = action,
                    actionSub = subAction
                )
            )
    }
}