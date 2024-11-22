package com.dashlane.pin.settings

import kotlinx.serialization.Serializable

sealed interface PinSettingsDestination {
    @Serializable
    data object SetupDestination : PinSettingsDestination

    @Serializable
    data class SuccessDestination(val pin: String) : PinSettingsDestination
}