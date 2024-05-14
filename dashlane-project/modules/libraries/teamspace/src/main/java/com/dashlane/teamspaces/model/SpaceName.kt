package com.dashlane.teamspaces.model

import androidx.annotation.StringRes

sealed class SpaceName {
    data class FixName(@StringRes val nameRes: Int) : SpaceName()

    data class TeamName(val value: String) : SpaceName()
}