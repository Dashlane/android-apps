package com.dashlane.notificationcenter

import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

class NotificationCenterLoggerImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    originProvider: NotificationCenterLogger.OriginProvider
) : NotificationCenterLogger {

    private val origin: String = originProvider.origin

    override fun logActionItemCenterShow() = log(
        action = "homepage",
        subAction = "show"
    )

    override fun logActionItemShow(key: String) = log(action = key, subAction = "show")

    override fun logActionItemClick(key: String) = log(action = key, subAction = "click")

    override fun logActionItemDismiss(key: String) = log(action = key, subAction = "dismiss")

    override fun logActionItemUndoDismiss(key: String) =
        log(action = key, subAction = "undo")

    private fun log(action: String, subAction: String = "") {
        bySessionUsageLogRepository[sessionManager.session]
            ?.enqueue(
                UsageLogCode75(
                    type = "action_items",
                    subtype = "announcement",
                    action = action,
                    subaction = subAction,
                    originStr = origin
                )
            )
    }
}