package com.dashlane.accountsummary.changelogin.verification

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.mvvm.State
import com.dashlane.udf.Event

data class EmailVerificationData(
    val newEmail: String,
    val token: TextFieldValue? = null,
    val isContinueEnabled: Boolean = false,
    val error: EmailVerificationError? = null,
    val showHelpDialog: Boolean = false,
) : State.View

sealed class EmailVerificationState : State {
    sealed class View : EmailVerificationState(), State.View {
        abstract val data: EmailVerificationData

        data class Initial(override val data: EmailVerificationData) : View()

        data class DisplayToken(override val data: EmailVerificationData) : View()

        data class DisplayHelpDialog(override val data: EmailVerificationData) : View()

        data class Loading(override val data: EmailVerificationData) : View()

        data class Success(override val data: EmailVerificationData, val isSessionValid: Boolean) : View()

        data class LoadingNewToken(override val data: EmailVerificationData) : View()

        data class Error(override val data: EmailVerificationData) : View()
    }

    sealed class SideEffect : EmailVerificationState(), State.SideEffect {
        data class Success(val isSessionValid: Boolean) : SideEffect()
    }
}

sealed class EmailVerificationEvent : Event {
    data class TokenChanged(val token: TextFieldValue) : EmailVerificationEvent()

    data object NextClicked : EmailVerificationEvent()

    data object HelpClicked : EmailVerificationEvent()

    data object BackPressed : EmailVerificationEvent()

    data object HelpDialogDismissed : EmailVerificationEvent()

    data object HelpDialogConfirmed : EmailVerificationEvent()

    data object Success : EmailVerificationEvent()

    data object SuccessClicked : EmailVerificationEvent()

    data object SuccessNewToken : EmailVerificationEvent()

    data class Error(val error: EmailVerificationError) : EmailVerificationEvent()

    data object Cancel : EmailVerificationEvent()
}

sealed class EmailVerificationError {
    data object Generic : EmailVerificationError()

    data object Network : EmailVerificationError()

    data object InvalidToken : EmailVerificationError()

    data object InvalidSession : EmailVerificationError()
}