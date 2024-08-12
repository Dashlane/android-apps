package com.dashlane.pin.settings.success

data class PinSettingsSuccessState(
    val isMPStoreDialogShown: Boolean = false
)

sealed class PinSettingsSuccessNavigationState {
    data object Success : PinSettingsSuccessNavigationState()
    data object Cancel : PinSettingsSuccessNavigationState()
}
