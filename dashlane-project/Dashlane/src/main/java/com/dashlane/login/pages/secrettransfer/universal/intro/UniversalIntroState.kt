package com.dashlane.login.pages.secrettransfer.universal.intro

import com.dashlane.mvvm.State
import com.dashlane.secrettransfer.domain.SecretTransferPayload
import com.dashlane.ui.widgets.compose.Passphrase

sealed class UniversalIntroState : State {

    sealed class View : UniversalIntroState(), State.View {

        data object Initial : View()
        data object LoadingPassphrase : View()
        data object LoadingAccount : View()
        data class PassphraseVerification(val passphrase: List<Passphrase>) : View()
        data class Error(val error: UniversalIntroError) : View()
    }

    sealed class SideEffect : UniversalIntroState(), State.SideEffect {
        data object GoToHelp : SideEffect()
        data object Cancel : SideEffect()
        data class Success(val secretTransferPayload: SecretTransferPayload) : SideEffect()
    }
}

sealed class UniversalIntroError {
    data object Generic : UniversalIntroError()
    data object Timeout : UniversalIntroError()
}
