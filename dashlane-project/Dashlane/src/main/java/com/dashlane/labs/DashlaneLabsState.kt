package com.dashlane.labs

sealed class DashlaneLabsState {
    abstract val viewData: ViewData

    data class Loading(override val viewData: ViewData) : DashlaneLabsState()
    data class Loaded(override val viewData: ViewData) : DashlaneLabsState()
}

data class ViewData(
    val features: List<String>,
    val helpClicked: Boolean
)