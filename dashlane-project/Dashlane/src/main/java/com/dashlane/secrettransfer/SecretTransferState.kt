package com.dashlane.secrettransfer

sealed class SecretTransferState {

    object Initial : SecretTransferState()
    object ScanningQR : SecretTransferState()
    object Loading : SecretTransferState()
    object Success : SecretTransferState()
    object Cancelled : SecretTransferState()
    data class Error(val error: SecretTransferError) : SecretTransferState()
}

sealed class SecretTransferError {
    object InvalidSession : SecretTransferError()
    object CryptographicError : SecretTransferError()
    object ServerError : SecretTransferError()
    object Generic : SecretTransferError()
}
