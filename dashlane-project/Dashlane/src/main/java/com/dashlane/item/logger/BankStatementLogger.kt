package com.dashlane.item.logger

import com.dashlane.inapplogin.UsageLogCode35Action
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode35

class BankStatementLogger(
    teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BaseLogger(teamspaceAccessor, dataCounter, sessionManager, bySessionUsageLogRepository) {

    fun logCopyBic() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.BANK_STATEMENT.code,
                action = UsageLogCode35Action.COPY_BIC
            )
        )
    }

    fun logCopyIban() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.BANK_STATEMENT.code,
                action = UsageLogCode35Action.COPY_IBAN
            )
        )
    }

    fun logRevealBic() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.BANK_STATEMENT.code,
                action = UsageLogCode35Action.SHOW_BIC
            )
        )
    }

    fun logRevealIban() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.BANK_STATEMENT.code,
                action = UsageLogCode35Action.SHOW_IBAN
            )
        )
    }
}