package com.dashlane.teamspaces.db

import androidx.annotation.VisibleForTesting
import com.dashlane.session.BySessionRepository
import com.dashlane.session.Session
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode105



class TeamspaceUsageLogSpaceChanged(
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val session: Session
) : TeamspaceManager.Listener {

    override fun onStatusChanged(
        teamspace: Teamspace?,
        previousStatus: String?,
        newStatus: String?
    ) {
        teamspace ?: return

        val action: UsageLogCode105.Action =
            if (Teamspace.Status.ACCEPTED == newStatus) {
                if (Teamspace.Status.REVOKED == previousStatus) { 
                    UsageLogCode105.Action.RE_ADD
                } else {
                    return 
                }
            } else if (Teamspace.Status.REVOKED == newStatus) {
                if (teamspace.shouldDeleteForceCategorizedContent()) {
                    UsageLogCode105.Action.DELETE
                } else { 
                    UsageLogCode105.Action.HIDE
                }
            } else {
                return 
            }
        sendUsageLog105(teamspace.anonTeamId, action)
    }

    override fun onChange(teamspace: Teamspace) { 
    }

    override fun onTeamspacesUpdate() { 
    }

    @VisibleForTesting
    fun sendUsageLog105(teamId: String?, action: UsageLogCode105.Action) {
        bySessionUsageLogRepository[session]?.enqueue(
            UsageLogCode105(
                action = action,
                type = UsageLogCode105.Type.TEAM_OFFER,
                spaceId = teamId
            )
        )
    }
}