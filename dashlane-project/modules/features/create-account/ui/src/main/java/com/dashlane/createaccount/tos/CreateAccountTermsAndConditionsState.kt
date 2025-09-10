package com.dashlane.createaccount.tos

import android.net.Uri
import com.dashlane.mvvm.State
import com.dashlane.udf.Event

data class CreateAccountTermsAndConditionsData(
    val isTosChecked: Boolean = false,
    val isOffersChecked: Boolean = false,
    val isContinueEnabled: Boolean = false,
    val error: CreateAccountTermsAndConditionsError? = null,
)

sealed class CreateAccountTermsAndConditionsState : State {
    sealed class View : CreateAccountTermsAndConditionsState(), State.View {
        abstract val data: CreateAccountTermsAndConditionsData

        data class Initial(override val data: CreateAccountTermsAndConditionsData) : View()

        data class Loading(override val data: CreateAccountTermsAndConditionsData) : View()
    }

    sealed class SideEffect : CreateAccountTermsAndConditionsState(), State.SideEffect {
        data class GoToHelp(val uri: Uri) : SideEffect()

        data object Success : SideEffect()
    }
}

sealed class CreateAccountTermsAndConditionsEvent : Event {
    data class TosCheckChanged(val isChecked: Boolean) : CreateAccountTermsAndConditionsEvent()

    data class OffersCheckChanged(val isChecked: Boolean) : CreateAccountTermsAndConditionsEvent()

    data object NextClicked : CreateAccountTermsAndConditionsEvent()

    data class HelpClicked(val uri: Uri) : CreateAccountTermsAndConditionsEvent()

    data object AccountCreated : CreateAccountTermsAndConditionsEvent()

    data class Error(val error: CreateAccountTermsAndConditionsError) : CreateAccountTermsAndConditionsEvent()
}

sealed class CreateAccountTermsAndConditionsError {
    data object Generic : CreateAccountTermsAndConditionsError()

    data object Network : CreateAccountTermsAndConditionsError()

    data object ExpiredVersion : CreateAccountTermsAndConditionsError()
}