package com.dashlane.teamspaces.manager

import com.dashlane.teamspaces.model.Teamspace
import java.lang.ref.WeakReference



class TeamspaceManagerWeakListener constructor(subListener: TeamspaceManager.Listener) : TeamspaceManager.Listener {

    private val subListenerRef: WeakReference<TeamspaceManager.Listener> = WeakReference(subListener)
    private var teamspaceManager: TeamspaceManager? = null

    fun listen(newTeamspaceManager: TeamspaceManager?) {
        val currentTeamspaceManager = teamspaceManager
        if (currentTeamspaceManager === newTeamspaceManager) {
            return 
        }
        currentTeamspaceManager?.unSubscribeListeners(this)
        teamspaceManager = newTeamspaceManager
        newTeamspaceManager?.subscribeListener(this)
    }

    override fun onChange(teamspace: Teamspace) {
        getListenerOrUnsubscribe()?.onChange(teamspace)
    }

    override fun onStatusChanged(teamspace: Teamspace?, previousStatus: String?, newStatus: String?) {
        getListenerOrUnsubscribe()?.onStatusChanged(teamspace, previousStatus, newStatus)
    }

    override fun onTeamspacesUpdate() {
        getListenerOrUnsubscribe()?.onTeamspacesUpdate()
    }

    private fun getListenerOrUnsubscribe(): TeamspaceManager.Listener? {
        val subListener = subListenerRef.get()
        if (subListener == null) {
            teamspaceManager?.unSubscribeListeners(this)
        }
        return subListener
    }
}