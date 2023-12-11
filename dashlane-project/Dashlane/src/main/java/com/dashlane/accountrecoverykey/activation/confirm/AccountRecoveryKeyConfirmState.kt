package com.dashlane.accountrecoverykey.activation.confirm

sealed class AccountRecoveryKeyConfirmState {
    abstract val data: AccountRecoveryKeyConfirmData

    data class Initial(override val data: AccountRecoveryKeyConfirmData) : AccountRecoveryKeyConfirmState()
    data class Loading(override val data: AccountRecoveryKeyConfirmData) : AccountRecoveryKeyConfirmState()
    data class KeyConfirmed(override val data: AccountRecoveryKeyConfirmData) : AccountRecoveryKeyConfirmState()
    data class Done(override val data: AccountRecoveryKeyConfirmData) : AccountRecoveryKeyConfirmState()
    data class Back(override val data: AccountRecoveryKeyConfirmData) : AccountRecoveryKeyConfirmState()
    data class Cancel(override val data: AccountRecoveryKeyConfirmData) : AccountRecoveryKeyConfirmState()
    data class SyncError(override val data: AccountRecoveryKeyConfirmData) : AccountRecoveryKeyConfirmState()
    data class KeyError(override val data: AccountRecoveryKeyConfirmData) : AccountRecoveryKeyConfirmState()
}

data class AccountRecoveryKeyConfirmData(
    val accountRecoveryKey: String? = null
)