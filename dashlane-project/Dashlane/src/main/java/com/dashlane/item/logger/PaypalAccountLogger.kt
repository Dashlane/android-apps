package com.dashlane.item.logger

import com.dashlane.inapplogin.UsageLogCode35Action
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode35

class PaypalAccountLogger(
    val teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BaseLogger(teamspaceAccessor, dataCounter, sessionManager, bySessionUsageLogRepository) {

    fun logCopyPaypalPassword() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_PAYPAL.code,
                action = UsageLogCode35Action.COPY_PASSWORD
            )
        )
    }

    fun logRevealPassword() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_PAYPAL.code,
                action = UsageLogCode35Action.SHOW_PASSWORD
            )
        )
    }
}