package com.dashlane.login.lock

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.dashlane.login.lock.OnboardingApplicationLockActivity.Companion.EXTRA_NEXT_INTENT
import com.dashlane.login.lock.OnboardingApplicationLockState.SideEffect
import com.dashlane.login.lock.OnboardingApplicationLockState.View
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class OnboardingApplicationLockViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, OnboardingApplicationLockEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : UDFViewModel<View, SideEffect, OnboardingApplicationLockEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        val nextIntent: Intent? = savedStateHandle[EXTRA_NEXT_INTENT]
        requireNotNull(nextIntent) {
            "Could not initial OnboardingApplicationLock due to missing required nextIntent arg"
        }
        start(createStateMachine(nextIntent))
    }

    internal fun createStateMachine(nextIntent: Intent) =
        StateMachine.create<View, OnboardingApplicationLockEvent, SideEffect> {
            initialState(View.Initial(OnboardingApplicationLockData(nextIntent = nextIntent)))
            state<View.Initial> {
                on<OnboardingApplicationLockEvent.ViewResumed> {
                    transitionTo(View.Loading(data))
                }
            }
            state<View.SuggestLockSetup> {
                on<OnboardingApplicationLockEvent.ViewResumed> {
                    transitionTo(View.Loading(data))
                }
                on<OnboardingApplicationLockEvent.SkipClicked> {
                    sideEffect(SideEffect.GoToNext(data.nextIntent))
                }
                on<OnboardingApplicationLockEvent.UseBiometricClicked> {
                    sideEffect(SideEffect.GoToBiometricActivation)
                }
                on<OnboardingApplicationLockEvent.UsePinCodeClicked> {
                    sideEffect(SideEffect.GoToPinCodeSetter)
                }
                on<OnboardingApplicationLockEvent.BiometricSetupDone> {
                    sideEffect(SideEffect.GoToNext(data.nextIntent))
                }
                on<OnboardingApplicationLockEvent.PinCodeSetupDone> {
                    sideEffect(SideEffect.GoToNext(data.nextIntent))
                }
            }
            state<View.Loading> {
                on<OnboardingApplicationLockEvent.ViewResumed> {
                    transitionTo(View.Loading(data))
                }
                on<OnboardingApplicationLockEvent.NoLockToSuggest> {
                    transitionTo(View.Initial(data), sideEffect = SideEffect.GoToNext(data.nextIntent))
                }
                on<OnboardingApplicationLockEvent.ShowLockSetupSuggestion> {
                    transitionTo(View.SuggestLockSetup(data.copy(hasBiometricHardware = it.hasBiometricHardware)))
                }
                on<OnboardingApplicationLockEvent.BiometricSetupDone> {
                    sideEffect(SideEffect.GoToNext(data.nextIntent))
                }
                on<OnboardingApplicationLockEvent.PinCodeSetupDone> {
                    sideEffect(SideEffect.GoToNext(data.nextIntent))
                }
            }
        }
}