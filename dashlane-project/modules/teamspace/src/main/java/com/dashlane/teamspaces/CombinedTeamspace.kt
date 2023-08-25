package com.dashlane.teamspaces

import com.dashlane.teamspaces.model.Teamspace

object CombinedTeamspace : Teamspace() {
    init {
        type = Type.COMBINED
        status = Status.ACCEPTED
        anonTeamId = ALL_SPACE_ANON_ID
    }
}