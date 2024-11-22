package com.dashlane.secrettransfer.domain

sealed class SecretTransferError {
    data object InvalidSession : SecretTransferError()
    data object CryptographicError : SecretTransferError()
    data object ServerError : SecretTransferError()
    data object Generic : SecretTransferError()
}
