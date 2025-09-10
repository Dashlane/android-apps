package com.dashlane.login.lock

import android.content.Intent
import com.dashlane.mvvm.State
import com.dashlane.udf.Event

data class OnboardingApplicationLockData(
    val nextIntent: Intent,
    val hasBiometricHardware: Boolean? = null,
)

sealed class OnboardingApplicationLockState : State {
    sealed class View : OnboardingApplicationLockState(), State.View {
        abstract val data: OnboardingApplicationLockData

        data class Initial(override val data: OnboardingApplicationLockData) : View()

        data class Loading(override val data: OnboardingApplicationLockData) : View()

        data class SuggestLockSetup(override val data: OnboardingApplicationLockData) : View()
    }

    sealed class SideEffect : OnboardingApplicationLockState(), State.SideEffect {
        data class GoToNext(val nextIntent: Intent) : SideEffect()

        data object GoToBiometricActivation : SideEffect()

        data object GoToPinCodeSetter : SideEffect()
    }
}

sealed class OnboardingApplicationLockEvent : Event {
    data object SkipClicked : OnboardingApplicationLockEvent()

    data object UseBiometricClicked : OnboardingApplicationLockEvent()

    data object BiometricSetupDone : OnboardingApplicationLockEvent()

    data object UsePinCodeClicked : OnboardingApplicationLockEvent()

    data object PinCodeSetupDone : OnboardingApplicationLockEvent()

    data object ViewResumed : OnboardingApplicationLockEvent()

    data class ShowLockSetupSuggestion(val hasBiometricHardware: Boolean) : OnboardingApplicationLockEvent()

    data object NoLockToSuggest : OnboardingApplicationLockEvent()
}