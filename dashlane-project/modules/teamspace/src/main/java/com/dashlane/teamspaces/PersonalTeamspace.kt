package com.dashlane.teamspaces

import com.dashlane.teamspaces.model.Teamspace

object PersonalTeamspace : Teamspace() {
    init {
        type = Type.PERSONAL
        status = Status.ACCEPTED
        anonTeamId = DEFAULT_SPACE_ANON_ID
        
        teamId = ""
    }
}