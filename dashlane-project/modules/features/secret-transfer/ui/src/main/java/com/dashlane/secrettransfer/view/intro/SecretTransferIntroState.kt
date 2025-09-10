package com.dashlane.secrettransfer.view.intro

import com.dashlane.mvvm.State
import com.dashlane.secrettransfer.domain.SecretTransferError

sealed class SecretTransferIntroState : State {
    data class View(
        val isLoading: Boolean = false,
        val error: SecretTransferError? = null,
        val isSuccess: Boolean = false,
    ) : SecretTransferIntroState(), State.View

    sealed class SideEffect : SecretTransferIntroState(), State.SideEffect {
        data object ScanningQR : SideEffect()

        data object Success : SideEffect()

        data object Cancelled : SideEffect()
    }
}