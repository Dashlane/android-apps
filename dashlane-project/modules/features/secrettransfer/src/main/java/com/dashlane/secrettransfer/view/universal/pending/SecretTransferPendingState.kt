package com.dashlane.secrettransfer.view.universal.pending

import com.dashlane.secrettransfer.view.SecretTransfer
import com.dashlane.ui.widgets.compose.Passphrase

sealed class SecretTransferPendingState {
    abstract val data: SecretTransferPendingData

    data class Initial(override val data: SecretTransferPendingData) : SecretTransferPendingState()
    data class LoadingChallenge(override val data: SecretTransferPendingData) : SecretTransferPendingState()
    data class LoadingAccount(override val data: SecretTransferPendingData) : SecretTransferPendingState()
    data class PassphraseVerification(override val data: SecretTransferPendingData) : SecretTransferPendingState()
    data class Reject(override val data: SecretTransferPendingData) : SecretTransferPendingState()
    data class CancelPassphrase(override val data: SecretTransferPendingData) : SecretTransferPendingState()
    data class Cancelled(override val data: SecretTransferPendingData) : SecretTransferPendingState()
    data class Error(override val data: SecretTransferPendingData, val error: SecretTransferPendingError) : SecretTransferPendingState()
    data class Success(override val data: SecretTransferPendingData) : SecretTransferPendingState()
}

data class SecretTransferPendingData(
    val transfer: SecretTransfer? = null,
    val passphrase: List<Passphrase>? = null,
    val passphraseTries: Int = 0
)

sealed class SecretTransferPendingError {
    data object Generic : SecretTransferPendingError()
    data object Timeout : SecretTransferPendingError()
    data object PassphraseMaxTries : SecretTransferPendingError()
}
