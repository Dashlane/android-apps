package com.dashlane.secrettransfer.qrcode

import android.graphics.Bitmap
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.mvvm.State
import com.dashlane.secrettransfer.domain.SecretTransferPayload

sealed class QrCodeState : State {
    data class View(
        val email: String? = null,
        val bottomSheetVisible: Boolean = false,
        val qrCodeBitmap: Bitmap? = null,
        val isLoading: Boolean = false,
        val error: QrCodeError? = null,
    ) : QrCodeState(), State.View

    sealed class SideEffect : QrCodeState(), State.SideEffect {
        data class GoToConfirmEmail(val secretTransferPayload: SecretTransferPayload) : SideEffect()
        data class GoToARK(val registeredUserDevice: RegisteredUserDevice.Local) : SideEffect()
        data class GoToUniversalD2D(val email: String) : SideEffect()
        data object Cancelled : SideEffect()
    }
}
sealed class QrCodeError {
    data object StartTransferError : QrCodeError()
    data object QrCodeGeneration : QrCodeError()
}
