package com.dashlane.login.pages.secrettransfer.qrcode

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.pages.secrettransfer.SecretTransferPayload

sealed class QrCodeState {
    abstract val data: QrCodeData

    data class Initial(override val data: QrCodeData) : QrCodeState()
    data class LoadingQR(override val data: QrCodeData) : QrCodeState()
    data class QrCodeUriGenerated(override val data: QrCodeData) : QrCodeState()
    data class GoToConfirmEmail(override val data: QrCodeData, val secretTransferPayload: SecretTransferPayload) : QrCodeState()
    data class GoToARK(override val data: QrCodeData, val registeredUserDevice: RegisteredUserDevice.Local) : QrCodeState()
    data class Cancelled(override val data: QrCodeData) : QrCodeState()
    data class Error(override val data: QrCodeData, val error: QrCodeError) : QrCodeState()
}

data class QrCodeData(
    val email: String? = null,
    val qrCodeUri: String? = null,
    val arkEnabled: Boolean = false
)

sealed class QrCodeError {
    object StartTransferError : QrCodeError()
    object QrCodeGeneration : QrCodeError()
}
