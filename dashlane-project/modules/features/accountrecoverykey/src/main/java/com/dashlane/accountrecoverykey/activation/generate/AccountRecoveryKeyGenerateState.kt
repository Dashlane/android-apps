package com.dashlane.accountrecoverykey.activation.generate

import com.dashlane.mvvm.State
import com.dashlane.user.UserAccountInfo

sealed class AccountRecoveryKeyGenerateState : State {
    data class View(
        val accountType: UserAccountInfo.AccountType = UserAccountInfo.AccountType.MasterPassword,
        val accountRecoveryKey: String? = null,
        val isLoading: Boolean = false,
        val userCanExitFlow: Boolean = false,
        val cancelDialogShown: Boolean = false,
        val error: AccountRecoveryKeyGenerateError? = null,
    ) : AccountRecoveryKeyGenerateState(), State.View

    sealed class SideEffect : AccountRecoveryKeyGenerateState(), State.SideEffect {
        data object GoToConfirm : SideEffect()
        data object Cancel : SideEffect()
    }
}

sealed class AccountRecoveryKeyGenerateError {
    data object Generic : AccountRecoveryKeyGenerateError()
}
