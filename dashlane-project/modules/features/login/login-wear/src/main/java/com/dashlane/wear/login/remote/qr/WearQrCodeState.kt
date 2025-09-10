package com.dashlane.wear.login.remote.qr

import android.graphics.Bitmap
import com.dashlane.mvvm.State
import com.dashlane.secrettransfer.domain.SecretTransferPayload

sealed class WearQrCodeState : State {
    sealed class View : WearQrCodeState(), State.View {
        abstract val isLogoVisible: Boolean

        data object Loading : View() {
            override val isLogoVisible: Boolean
                get() = true
        }

        data class QrCode(val qrCodeBitmap: Bitmap? = null) : View() {
            override val isLogoVisible: Boolean
                get() = false
        }

        data class EmailConfirmation(val secretTransferPayload: SecretTransferPayload) : View() {
            override val isLogoVisible: Boolean
                get() = true
        }

        data class Error(val error: QrCodeError? = null) : View() {
            override val isLogoVisible: Boolean
                get() = false
        }
    }

    sealed class SideEffect : WearQrCodeState(), State.SideEffect {
        data object GoToOtpChallenge : SideEffect()

        data object GoToAuthorization : SideEffect()
    }
}

sealed class QrCodeError {
    data object StartTransferError : QrCodeError()

    data class QrCodeGenerationError(val isDeviceOffline: Boolean) : QrCodeError()

    data class AccountVerificationError(val throwable: Throwable) : QrCodeError()
}