package com.dashlane.secrettransfer.view.intro

import com.dashlane.secrettransfer.SecretTransferError

sealed class SecretTransferIntroState {

    data object Initial : SecretTransferIntroState()
    data object ScanningQR : SecretTransferIntroState()
    data object Loading : SecretTransferIntroState()
    data object Success : SecretTransferIntroState()
    data object Cancelled : SecretTransferIntroState()
    data class Error(val error: SecretTransferError) : SecretTransferIntroState()
}
