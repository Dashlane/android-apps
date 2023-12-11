package com.dashlane.createaccount.passwordless.confirmation

import com.dashlane.R

sealed class ConfirmationState {
    object Initial : ConfirmationState()
    object Loading : ConfirmationState()
    object AccountCreated : ConfirmationState()
    sealed class Error : ConfirmationState() {
        abstract val messageRes: Int

        data class NetworkError(override val messageRes: Int = R.string.network_error) : Error()
        data class ExpiredVersion(override val messageRes: Int = R.string.error) : Error()
        data class UnknownError(override val messageRes: Int = R.string.error) : Error()
    }
}