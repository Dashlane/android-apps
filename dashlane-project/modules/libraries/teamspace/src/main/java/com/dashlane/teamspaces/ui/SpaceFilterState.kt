package com.dashlane.teamspaces.ui

import com.dashlane.teamspaces.model.TeamSpace

sealed class SpaceFilterState {
    abstract val teamSpace: TeamSpace

    data object Init : SpaceFilterState() {
        override val teamSpace: TeamSpace
            get() = TeamSpace.Combined
    }

    data class Loaded(override val teamSpace: TeamSpace) : SpaceFilterState()
}