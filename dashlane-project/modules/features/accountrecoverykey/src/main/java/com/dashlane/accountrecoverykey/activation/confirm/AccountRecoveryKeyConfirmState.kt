package com.dashlane.accountrecoverykey.activation.confirm

import com.dashlane.mvvm.State

sealed class AccountRecoveryKeyConfirmState : State {
    data class View(
        val accountRecoveryKey: String? = null,
        val isLoading: Boolean = false,
        val error: AccountRecoveryKeyConfirmError? = null,
    ) : AccountRecoveryKeyConfirmState(), State.View

    sealed class SideEffect : AccountRecoveryKeyConfirmState(), State.SideEffect {
        data object KeyConfirmed : SideEffect()
        data object Back : SideEffect()
        data object Cancel : SideEffect()
    }
}

sealed class AccountRecoveryKeyConfirmError {
    data object SyncError : AccountRecoveryKeyConfirmError()
    data object KeyError : AccountRecoveryKeyConfirmError()
}
