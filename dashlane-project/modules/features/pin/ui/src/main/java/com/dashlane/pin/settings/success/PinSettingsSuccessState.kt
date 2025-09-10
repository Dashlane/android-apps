package com.dashlane.pin.settings.success

import com.dashlane.mvvm.State
import com.dashlane.udf.Event

data class PinSettingsSuccessData(
    val pin: String,
    val isMPStoreDialogShown: Boolean = false,
) : State.View

sealed class PinSettingsSuccessState : State {
    sealed class View : PinSettingsSuccessState(), State.View {
        abstract val data: PinSettingsSuccessData

        data class Initial(override val data: PinSettingsSuccessData) : View()

        data class DisplayStoreMPDialog(override val data: PinSettingsSuccessData) : View()

        data class Loading(override val data: PinSettingsSuccessData) : View()

        data class Success(override val data: PinSettingsSuccessData) : View()
    }

    sealed class SideEffect : PinSettingsSuccessState(), State.SideEffect {
        data object Success : SideEffect()

        data object Cancel : SideEffect()
    }
}

sealed class PinSettingsSuccessEvent : Event {
    data object PinSaved : PinSettingsSuccessEvent()

    data object CredentialsNotSaved : PinSettingsSuccessEvent()

    data object DialogConfirmed : PinSettingsSuccessEvent()

    data object DialogDismissed : PinSettingsSuccessEvent()

    data object BackPressed : PinSettingsSuccessEvent()

    data object Cancel : PinSettingsSuccessEvent()
}