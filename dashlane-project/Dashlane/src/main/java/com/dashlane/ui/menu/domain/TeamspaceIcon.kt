package com.dashlane.ui.menu.domain

sealed class TeamspaceIcon {
    object Combined : TeamspaceIcon()
    data class Space(val displayLetter: String, val colorInt: Int) : TeamspaceIcon()
}