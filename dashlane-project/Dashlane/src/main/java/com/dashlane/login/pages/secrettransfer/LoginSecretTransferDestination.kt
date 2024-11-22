package com.dashlane.login.pages.secrettransfer

import kotlinx.serialization.Serializable

sealed interface LoginSecretTransferDestination {

    @Serializable
    data object ChooseType : LoginSecretTransferDestination

    @Serializable
    data class UniversalIntro(val email: String? = null) : LoginSecretTransferDestination

    @Serializable
    data class RecoveryHelp(val email: String? = null) : LoginSecretTransferDestination

    @Serializable
    data object LostKey : LoginSecretTransferDestination

    @Serializable
    data class QrCode(val email: String? = null) : LoginSecretTransferDestination

    @Serializable
    data class ConfirmEmail(val email: String) : LoginSecretTransferDestination

    @Serializable
    data object Authorize : LoginSecretTransferDestination
}