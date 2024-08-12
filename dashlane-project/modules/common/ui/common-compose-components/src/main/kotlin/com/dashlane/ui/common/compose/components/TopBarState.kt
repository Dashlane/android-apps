package com.dashlane.ui.common.compose.components

import java.io.Serializable

data class TopBarState(
    val visible: Boolean = true,
    val title: String = "",
    val backEnabled: Boolean = true,
) : Serializable