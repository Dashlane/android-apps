package com.dashlane.accountsummary.changelogin

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.mvvm.State
import com.dashlane.udf.Event

data class ChangeLoginEmailData(
    val error: ChangeLoginEmailError? = null,
    val isContinueEnabled: Boolean = false,
    val currentLoginEmail: String? = null,
    val newLoginEmail: TextFieldValue? = null,
) : State.View

sealed class ChangeLoginEmailState : State {
    sealed class View : ChangeLoginEmailState(), State.View {
        abstract val data: ChangeLoginEmailData

        data class Initial(override val data: ChangeLoginEmailData) : View()

        data class DisplayEmail(override val data: ChangeLoginEmailData) : View()

        data class DisplayNewEmail(override val data: ChangeLoginEmailData) : View()

        data class Loading(override val data: ChangeLoginEmailData) : View()

        data class Error(override val data: ChangeLoginEmailData) : View()
    }

    sealed class SideEffect : ChangeLoginEmailState(), State.SideEffect {
        data class Next(val newEmail: String) : SideEffect()

        data object Finish : SideEffect()
    }
}

sealed class ChangeLoginEmailEvent : Event {
    data class CurrentEmailLoaded(val email: String) : ChangeLoginEmailEvent()

    data class NewEmailChanged(val newEmail: TextFieldValue) : ChangeLoginEmailEvent()

    data object NextClicked : ChangeLoginEmailEvent()

    data class EmailValid(val newEmail: String) : ChangeLoginEmailEvent()

    data class Error(val error: ChangeLoginEmailError) : ChangeLoginEmailEvent()

    data object Cancel : ChangeLoginEmailEvent()
}

sealed class ChangeLoginEmailError : State.SideEffect {
    data object Network : ChangeLoginEmailError()

    data object Generic : ChangeLoginEmailError()

    data object EmailUnavailable : ChangeLoginEmailError()

    data object InvalidEmail : ChangeLoginEmailError()
}