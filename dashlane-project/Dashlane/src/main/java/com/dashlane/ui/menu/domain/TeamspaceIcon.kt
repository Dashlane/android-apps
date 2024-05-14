package com.dashlane.ui.menu.domain

import com.dashlane.teamspaces.model.SpaceColor

sealed class TeamspaceIcon {
    data object Combined : TeamspaceIcon()
    data class Space(val displayLetter: Char, val spaceColor: SpaceColor) : TeamspaceIcon()
}