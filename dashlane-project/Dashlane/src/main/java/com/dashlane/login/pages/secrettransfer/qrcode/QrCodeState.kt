package com.dashlane.login.pages.secrettransfer.qrcode

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.secrettransfer.domain.SecretTransferPayload

sealed class QrCodeState {
    abstract val data: QrCodeData

    data class Initial(override val data: QrCodeData) : QrCodeState()
    data class LoadingQR(override val data: QrCodeData) : QrCodeState()
    data class QrCodeUriGenerated(override val data: QrCodeData) : QrCodeState()
    data class GoToConfirmEmail(override val data: QrCodeData, val secretTransferPayload: SecretTransferPayload) : QrCodeState()
    data class GoToARK(override val data: QrCodeData, val registeredUserDevice: RegisteredUserDevice.Local) : QrCodeState()
    data class GoToUniversalD2D(override val data: QrCodeData, val email: String) : QrCodeState()
    data class Cancelled(override val data: QrCodeData) : QrCodeState()
    data class Error(override val data: QrCodeData, val error: QrCodeError) : QrCodeState()
}

data class QrCodeData(
    val email: String? = null,
    val bottomSheetVisible: Boolean = false,
    val qrCodeUri: String? = null
)

sealed class QrCodeError {
    data object StartTransferError : QrCodeError()
    data object QrCodeGeneration : QrCodeError()
}
