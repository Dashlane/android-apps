package com.dashlane.ui.menu

import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.hermes.generated.events.user.SelectSpace
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.manager.TeamspaceManagerWeakListener
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.activities.fragments.checklist.ChecklistHelper
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.canShowVpn
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

open class MenuDataProvider @Inject constructor(
    override val menuUsageLogger: MenuUsageLogger,
    private val userFeature: UserFeaturesChecker,
    private val checklistHelper: ChecklistHelper,
    private val logRepository: LogRepository,
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val sessionManager: SessionManager
) : BaseDataProvider<MenuDef.IPresenter?>(), MenuDef.IDataProvider, TeamspaceManager.Listener {

    val teamspaceManager: TeamspaceManager?
        get() = teamspaceRepository[sessionManager.session!!]

    init {
        listenTeamspaceManager()
    }

    override val teamspaces: List<Teamspace>
        get() {
            (teamspaceManager?.all?.toMutableList() ?: mutableListOf()).remove(teamspaceManager?.current)
            return teamspaceManager?.all?.toMutableList() ?: mutableListOf()
        }

    override fun onTeamspaceSelected(teamspace: Teamspace?) {
        if (teamspace != teamspaceManager?.current) {
            menuUsageLogger.logSpaceChange(teamspace)
            logRepository.queueEvent(SelectSpace(toSpace(teamspace!!)))
        }
        teamspaceManager?.current = teamspace
    }

    override val isVPNVisible: Boolean
        get() = userFeature.canShowVpn()
    override val isPersonalPlanVisible: Boolean
        get() = checklistHelper.shouldDisplayChecklist()

    override fun onStatusChanged(teamspace: Teamspace?, previousStatus: String?, newStatus: String?) {
        
    }

    override fun onChange(teamspace: Teamspace?) {
        refreshMenu()
    }

    override fun onTeamspacesUpdate() {
        refreshMenu()
    }

    private fun refreshMenu() {
        presenter.refreshMenuList()
    }

    private fun listenTeamspaceManager() {
        val listener = TeamspaceManagerWeakListener(this)
        listener.listen(teamspaceManager)
    }

    companion object {
        private fun toSpace(teamspace: Teamspace): Space {
            return when (teamspace.type) {
                Teamspace.Type.PERSONAL -> Space.PERSONAL
                Teamspace.Type.COMBINED -> Space.ALL
                Teamspace.Type.COMPANY -> Space.PROFESSIONAL
                else -> throw IllegalStateException("Unhandled Teamspace type " + teamspace.type)
            }
        }
    }
}