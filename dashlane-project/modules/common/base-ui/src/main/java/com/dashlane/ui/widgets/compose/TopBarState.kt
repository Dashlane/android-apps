package com.dashlane.ui.widgets.compose

import java.io.Serializable

data class TopBarState(
    val visible: Boolean = true,
    val title: String = ""
) : Serializable