package com.dashlane.autofill.smsotp

import com.dashlane.mvvm.State

sealed class SmsOtpState : State {
    sealed class View : SmsOtpState(), State.View {
        data object Initial : View()

        sealed class Shown : View() {
            data object Loading : Shown()

            data class DisplayCode(val smsCode: String) : Shown()

            data class Error(val message: String) : Shown()
        }

        data object Hidden : View()
    }

    sealed class SideEffect : SmsOtpState(), State.SideEffect {
        data class OnSuccess(val smsCode: String) : SideEffect()

        data object OnCancel : SideEffect()
    }
}