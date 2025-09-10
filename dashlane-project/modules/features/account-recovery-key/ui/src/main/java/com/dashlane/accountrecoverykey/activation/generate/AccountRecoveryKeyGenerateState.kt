package com.dashlane.accountrecoverykey.activation.generate

import com.dashlane.mvvm.State
import com.dashlane.udf.Event
import com.dashlane.user.UserAccountInfo

data class AccountRecoveryKeyGenerateData(
    val accountType: UserAccountInfo.AccountType = UserAccountInfo.AccountType.MasterPassword,
    val accountRecoveryKey: String? = null,
    val userCanExitFlow: Boolean = false,
    val error: AccountRecoveryKeyGenerateError? = null,
)

sealed class AccountRecoveryKeyGenerateState : State.View {
    abstract val data: AccountRecoveryKeyGenerateData

    data class Initial(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()

    data class Loading(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()

    data class DisplayARK(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()

    data class Cancelling(override val data: AccountRecoveryKeyGenerateData) : AccountRecoveryKeyGenerateState()
}

sealed class AccountRecoveryKeyGenerateSideEffect : State.SideEffect {
    data class Copying(val formattedAccountRecoveryKey: String) : AccountRecoveryKeyGenerateSideEffect()

    data object GoToConfirm : AccountRecoveryKeyGenerateSideEffect()

    data object Cancel : AccountRecoveryKeyGenerateSideEffect()
}

sealed class AccountRecoveryKeyGenerateEvent : Event {
    data class ViewStarted(val userCanExitFlow: Boolean) : AccountRecoveryKeyGenerateEvent()

    data class ArkActivated(
        val accountType: UserAccountInfo.AccountType,
        val formattedAccountRecoveryKey: String,
    ) : AccountRecoveryKeyGenerateEvent()

    data class Error(val error: AccountRecoveryKeyGenerateError) : AccountRecoveryKeyGenerateEvent()

    data object Retry : AccountRecoveryKeyGenerateEvent()

    data object Copy : AccountRecoveryKeyGenerateEvent()

    data object Continue : AccountRecoveryKeyGenerateEvent()

    data object BackPressed : AccountRecoveryKeyGenerateEvent()

    data object CancelDialogDismissed : AccountRecoveryKeyGenerateEvent()

    data object CancelConfirmed : AccountRecoveryKeyGenerateEvent()
}

sealed class AccountRecoveryKeyGenerateError {
    data object Generic : AccountRecoveryKeyGenerateError()
}