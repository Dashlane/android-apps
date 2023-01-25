package com.dashlane.security.darkwebmonitoring.item

import com.dashlane.darkweb.DarkWebEmailStatus
import com.dashlane.security.darkwebmonitoring.item.DarkwebMonitoringLogger.Companion.DARK_WEB_MODULE_ORIGIN
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode129
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class DarkwebMonitoringLoggerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : DarkwebMonitoringLogger {

    override fun onInactiveDarkwebModuleShow() {
        log129(
            UsageLogCode129.Type.IDENTITY_DASHBOARD,
            DARK_WEB_MODULE_ORIGIN,
            UsageLogCode129.Action.SHOW
        )
    }

    override fun onInactiveClickRegisterEmail() {
        log129(
            UsageLogCode129.Type.IDENTITY_DASHBOARD,
            DARK_WEB_MODULE_ORIGIN,
            UsageLogCode129.Action.ADD_EMAIL
        )
    }

    override fun onClickRemoveEmail(item: DarkWebEmailStatus) {
        val subAction = when (item.status) {
            DarkWebEmailStatus.STATUS_ACTIVE -> "remove_active_address"
            DarkWebEmailStatus.STATUS_DISABLED -> "remove_inactive_address"
            DarkWebEmailStatus.STATUS_PENDING -> "remove_pending_address"
            else -> null
        }

        log129(
            UsageLogCode129.Type.IDENTITY_DASHBOARD,
            action = UsageLogCode129.Action.CLICK,
            actionSub = subAction
        )
    }

    private fun log129(
        type: UsageLogCode129.Type,
        typeSub: String? = null,
        action: UsageLogCode129.Action? = null,
        actionSub: String? = null
    ) {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode129(
                    type = type,
                    typeSub = typeSub,
                    action = action,
                    actionSub = actionSub

                )
            )
    }
}