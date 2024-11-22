package com.dashlane.changemasterpassword.success

import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.mvvm.State

sealed class ChangeMasterPasswordSuccessState : State {
    data class View(
        val obfuscatedMasterPassword: ObfuscatedByteArray? = null,
        val progress: Int = 0,
        val hasFinishedLoading: Boolean = false,
        val showReminderDialog: Boolean = false,
        val error: ChangeMasterPasswordSuccessError? = null,
    ) : ChangeMasterPasswordSuccessState(), State.View

    sealed class SideEffect : ChangeMasterPasswordSuccessState(), State.SideEffect {
        data object NavigateBack : SideEffect()
        data object Success : SideEffect()
        data object Cancel : SideEffect()
        data object Logout : SideEffect()
    }
}

sealed class ChangeMasterPasswordSuccessError {
    data object Generic : ChangeMasterPasswordSuccessError()
    data object CompletedButSyncError : ChangeMasterPasswordSuccessError()
}
