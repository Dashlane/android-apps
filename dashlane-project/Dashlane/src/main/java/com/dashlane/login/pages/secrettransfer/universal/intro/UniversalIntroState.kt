package com.dashlane.login.pages.secrettransfer.universal.intro

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.ui.widgets.compose.Passphrase

sealed class UniversalIntroState {
    abstract val data: UniversalIntroData

    data class Initial(override val data: UniversalIntroData) : UniversalIntroState()
    data class GoToHelp(override val data: UniversalIntroData) : UniversalIntroState()
    data class LoadingPassphrase(override val data: UniversalIntroData) : UniversalIntroState()
    data class LoadingAccount(override val data: UniversalIntroData) : UniversalIntroState()
    data class PassphraseVerification(override val data: UniversalIntroData) : UniversalIntroState()
    data class Cancel(override val data: UniversalIntroData) : UniversalIntroState()
    data class Success(
        override val data: UniversalIntroData,
        val secretTransferPayload: SecretTransferPayload,
        val registeredUserDevice: RegisteredUserDevice.Remote
    ) : UniversalIntroState()

    data class Error(override val data: UniversalIntroData, val error: UniversalIntroError) : UniversalIntroState()
}

data class UniversalIntroData(
    val email: String? = null,
    val transferId: String? = null,
    val passphrase: List<Passphrase>? = null
)

sealed class UniversalIntroError {
    data object Generic : UniversalIntroError()
    data object Timeout : UniversalIntroError()
}
