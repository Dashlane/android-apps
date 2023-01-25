package com.dashlane.ui.screens.settings

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode35

class WarningRememberMasterPasswordDialogLogger(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {

    fun logUsageLog35(usageLog35Type: String, action: String) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode35(
                    type = usageLog35Type,
                    action = action
                )
            )
    }
}