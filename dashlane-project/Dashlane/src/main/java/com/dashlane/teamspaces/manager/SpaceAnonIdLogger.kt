package com.dashlane.teamspaces.manager

import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode105



class SpaceAnonIdLogger(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val session: Session
) {

    fun logSpace(type: UsageLogCode105.Type, spaceId: String?) {
        bySessionUsageLogRepository[session]?.enqueue(
            UsageLogCode105(
                action = UsageLogCode105.Action.ADD,
                spaceId = spaceId,
                type = type
            )
        )
    }
}