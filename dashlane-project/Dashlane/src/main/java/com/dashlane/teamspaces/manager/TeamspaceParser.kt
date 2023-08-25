package com.dashlane.teamspaces.manager

import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.teamspaces.model.Teamspace
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

object TeamspaceParser {
    fun deserializeTeamspaces(teamspaces: String?): List<Teamspace> {
        return teamspaces?.let {
            try {
                Gson().fromJson(teamspaces, Array<Teamspace>::class.java)?.asList()
            } catch (e: JsonSyntaxException) {
                DeveloperUtilities.throwRuntimeExceptionDebug(SingletonProvider.getContext(), e.message)
                null
            }
        } ?: emptyList()
    }
}