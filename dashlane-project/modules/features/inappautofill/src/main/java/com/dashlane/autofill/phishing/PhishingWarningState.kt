package com.dashlane.autofill.phishing

import com.dashlane.mvvm.State

sealed class PhishingWarningState : State {
    sealed class ViewState : PhishingWarningState(), State.View {
        data object Initial : ViewState()

        data class Loaded(
            val suspiciousWebsite: String,
            val itemWebsite: String,
        ) : ViewState()
    }

    sealed class SideEffect : PhishingWarningState(), State.SideEffect {
        data class OnResult(val shouldAutofill: Boolean) : SideEffect()
    }
}