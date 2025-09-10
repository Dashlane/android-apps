package com.dashlane.createaccount.email

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.authentication.create.AccountCreationEmailRepository
import com.dashlane.data.login.SsoInfo
import com.dashlane.mvvm.State
import com.dashlane.sso.login.RemoteLoginSsoActivity
import com.dashlane.udf.Event

data class CreateAccountEmailData(
    val email: TextFieldValue = TextFieldValue(""),
    val isContinueEnabled: Boolean = false,
    val error: CreateAccountEmailError? = null,
    val result: AccountCreationEmailRepository.Result.Success? = null,
)

sealed class CreateAccountEmailState : State {
    sealed class View : CreateAccountEmailState(), State.View {
        abstract val data: CreateAccountEmailData

        data class Initial(override val data: CreateAccountEmailData) : View()

        data class DisplayEmail(override val data: CreateAccountEmailData) : View()

        data class Loading(override val data: CreateAccountEmailData) : View()

        data class Error(override val data: CreateAccountEmailData) : View()

        data class DisplayValidateEmailDialog(override val data: CreateAccountEmailData) : View()

        data class DisplaySsoAdminErrorDialog(override val data: CreateAccountEmailData) : View()

        data class DisplayExpiredVersionErrorDialog(override val data: CreateAccountEmailData) : View()
    }

    sealed class SideEffect : CreateAccountEmailState(), State.SideEffect {
        data class GoToNext(val email: String, val isB2B: Boolean) : SideEffect()

        data object GoToHelp : SideEffect()

        data class GoToSso(val ssoInfo: SsoInfo) : SideEffect()

        data object Cancel : SideEffect()
    }
}

sealed class CreateAccountEmailEvent : Event {
    data class EmailChanged(val email: TextFieldValue) : CreateAccountEmailEvent()

    data object ContinueClicked : CreateAccountEmailEvent()

    data class EmailUnlikelyValid(val result: AccountCreationEmailRepository.Result.Success) : CreateAccountEmailEvent()

    data object CancelDialog : CreateAccountEmailEvent()

    data object ConfirmEmail : CreateAccountEmailEvent()

    data class EmailValid(val email: String, val isB2B: Boolean) : CreateAccountEmailEvent()

    data class SsoLoginComplete(val result: RemoteLoginSsoActivity.Result) : CreateAccountEmailEvent()

    data class Error(val error: CreateAccountEmailError) : CreateAccountEmailEvent()

    data class AccountIsSso(val ssoInfo: SsoInfo) : CreateAccountEmailEvent()

    data object HelpClicked : CreateAccountEmailEvent()
}

sealed class CreateAccountEmailError {
    data object Generic : CreateAccountEmailError()

    data object EmptyEmail : CreateAccountEmailError()

    data object InvalidEmail : CreateAccountEmailError()

    data object AlreadyExist : CreateAccountEmailError()

    data object AuthenticationExpiredVersion : CreateAccountEmailError()

    data object Sso : CreateAccountEmailError()

    data object AdminSso : CreateAccountEmailError()

    data object Network : CreateAccountEmailError()
}