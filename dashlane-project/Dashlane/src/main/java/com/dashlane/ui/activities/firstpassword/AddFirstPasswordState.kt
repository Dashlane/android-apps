package com.dashlane.ui.activities.firstpassword

import com.dashlane.mvvm.State
import com.dashlane.xml.domain.SyncObfuscatedValue

sealed class AddFirstPasswordState : State {
    data class View(
        val urlForThumbnail: String,
        val email: String,
        val password: String = "",
        val isPasswordObfuscated: Boolean = true,
        val isSaveButtonEnabled: Boolean = false,
        val showFAQ: Boolean = false,
        val showAutofillDemoPrompt: Boolean = false,
    ) : AddFirstPasswordState(), State.View

    sealed class SideEffect : AddFirstPasswordState(), State.SideEffect {
        data object FinishOnError : SideEffect()

        data object GoToHomeOrChecklist : SideEffect()

        data class GoToAutofillDemo(val url: String, val login: String, val password: SyncObfuscatedValue) : SideEffect()
    }
}