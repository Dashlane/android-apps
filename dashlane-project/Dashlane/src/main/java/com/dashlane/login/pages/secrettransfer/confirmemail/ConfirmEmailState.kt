package com.dashlane.login.pages.secrettransfer.confirmemail

import com.dashlane.authentication.RegisteredUserDevice

sealed class ConfirmEmailState {
    abstract val data: ConfirmEmailData

    data class ConfirmEmail(override val data: ConfirmEmailData) : ConfirmEmailState()
    data class AskForTOTP(override val data: ConfirmEmailData) : ConfirmEmailState()
    data class WaitForPush(override val data: ConfirmEmailData) : ConfirmEmailState()
    data class RegisterSuccess(override val data: ConfirmEmailData, val registeredUserDevice: RegisteredUserDevice.Remote) : ConfirmEmailState()
    data class Cancelled(override val data: ConfirmEmailData) : ConfirmEmailState()
    data class Error(override val data: ConfirmEmailData) : ConfirmEmailState()
}

data class ConfirmEmailData(
    val email: String
)
