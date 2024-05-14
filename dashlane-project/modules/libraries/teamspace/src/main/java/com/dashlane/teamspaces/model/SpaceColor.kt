package com.dashlane.teamspaces.model

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

sealed class SpaceColor {
    data class FixColor(@ColorRes val colorRes: Int) : SpaceColor()

    data class TeamColor(@ColorInt val color: Int) : SpaceColor()
}
