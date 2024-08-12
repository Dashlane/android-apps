package com.dashlane.labs

sealed class DashlaneLabsState {
    abstract val viewData: ViewData

    data class Loading(override val viewData: ViewData) : DashlaneLabsState()
    data class Loaded(override val viewData: ViewData) : DashlaneLabsState()
}

data class ViewData(
    val labFeatures: List<Lab>,
    val helpClicked: Boolean
) {
    data class Lab(
        val featureName: String,
        val displayDescription: String,
        val displayName: String,
        val enabled: Boolean
    )
}