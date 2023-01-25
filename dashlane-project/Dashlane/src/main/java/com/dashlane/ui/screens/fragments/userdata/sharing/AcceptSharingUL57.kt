package com.dashlane.ui.screens.fragments.userdata.sharing

import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.useractivity.log.usage.UsageLogCode57
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.fillAndSend
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject



class AcceptSharingUL57 @Inject constructor(
    private val sessionManager: SessionManager,
    private val teamspaceManagerRepository: TeamspaceManagerRepository,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) {
    private val session: Session?
        get() = sessionManager.session

    fun send(summaryObject: SummaryObject) {
        val session = session ?: return
        if (summaryObject !is SummaryObject.Authentifiant) return
        val anonTeamId = teamspaceManagerRepository[session]?.current?.anonTeamId ?: return
        val usageLogRepository = bySessionUsageLogRepository[session] ?: return

        UsageLogCode57(
            spaceId = anonTeamId,
            action = UsageLogCode57.Action.ADD,
            sender = UsageLogCode57.Sender.ADVANCED_SHARING
        ).fillAndSend(usageLogRepository, summaryObject, true)
    }
}