package com.dashlane.ui.activities.fragments.vault

import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.model.Teamspace
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

class VaultDataProvider @Inject constructor(
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val sessionManager: SessionManager
) : BaseDataProvider<Vault.Presenter>(), Vault.DataProvider, TeamspaceManager.Listener {

    val teamspaceManager: TeamspaceManager?
        get() = sessionManager.session?.let { teamspaceRepository.getTeamspaceManager(it) }

    override fun subscribeTeamspaceManager() {
        teamspaceManager?.subscribeListener(this)
    }

    override fun unsubscribeTeamspaceManager() {
        teamspaceManager?.unSubscribeListeners(this)
    }

    override fun onChange(teamspace: Teamspace?) {
        presenter.onTeamspaceChange(teamspace)
    }

    override fun onStatusChanged(teamspace: Teamspace?, previousStatus: String?, newStatus: String?) {
        
    }

    override fun onTeamspacesUpdate() {
        
    }
}