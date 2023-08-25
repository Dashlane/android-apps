package com.dashlane.teamspaces.manager

import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.tryOrNull
import javax.inject.Inject

class TeamspaceAccessorProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository
) : OptionalProvider<TeamspaceAccessor> {

    override fun get(): TeamspaceAccessor? {
        return tryOrNull { teamspaceRepository.getTeamspaceManager(sessionManager.session!!) }
    }
}