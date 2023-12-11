package com.dashlane.teamspaces.manager

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.teamspaces.db.TeamspaceForceCategorizationManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.tryOrNull
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject

class TeamspaceUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val teamspaceManagerRepository: TeamspaceManagerRepository,
    private val forceCategorizationManager: Lazy<TeamspaceForceCategorizationManager>
) {

    fun processAndSaveTeamspaces(teamspaces: JSONArray?) {
        teamspaces ?: return 
        sessionManager.session?.let { teamspaceManagerRepository.getTeamspaceManager(it) }
            ?.let { teamspaceManager ->
                tryOrNull { deserializeTeamspaces(teamspaces) }
                    ?.let { teamspaceManager.init(it, forceCategorizationManager.get()) }
            }
    }

    fun deserializeTeamspaces(teamspaces: JSONArray?): List<Teamspace> {
        return deserializeTeamspaces(teamspaces?.toString())
    }

    @VisibleForTesting
    fun deserializeTeamspaces(teamspaces: String?): List<Teamspace> {
        return TeamspaceParser.deserializeTeamspaces(context, teamspaces)
    }
}
