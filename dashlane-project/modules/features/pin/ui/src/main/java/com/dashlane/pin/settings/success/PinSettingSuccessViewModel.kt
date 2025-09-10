package com.dashlane.pin.settings.success

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.dashlane.pin.settings.PinSettingsDestination.SuccessDestination
import com.dashlane.pin.settings.success.PinSettingsSuccessEvent.BackPressed
import com.dashlane.pin.settings.success.PinSettingsSuccessEvent.CredentialsNotSaved
import com.dashlane.pin.settings.success.PinSettingsSuccessEvent.DialogConfirmed
import com.dashlane.pin.settings.success.PinSettingsSuccessEvent.DialogDismissed
import com.dashlane.pin.settings.success.PinSettingsSuccessEvent.PinSaved
import com.dashlane.pin.settings.success.PinSettingsSuccessState.SideEffect
import com.dashlane.pin.settings.success.PinSettingsSuccessState.View
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class PinSettingSuccessViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, PinSettingsSuccessEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : UDFViewModel<View, SideEffect, PinSettingsSuccessEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        val pinSettingsSuccessRoute = savedStateHandle.toRoute<SuccessDestination>()
        val initialState = View.Initial(PinSettingsSuccessData(pinSettingsSuccessRoute.pin))

        start(createStateMachine(initialState))
    }

    @Suppress("LongMethod")
    internal fun createStateMachine(initialState: View) = StateMachine.create<View, PinSettingsSuccessEvent, SideEffect> {
        initialState(initialState)
        state<View.Initial> {
            on<PinSaved> {
                transitionTo(View.Success(data), SideEffect.Success)
            }
            on<CredentialsNotSaved> {
                transitionTo(View.DisplayStoreMPDialog(data))
            }
            on<BackPressed> {
                sideEffect(SideEffect.Cancel)
            }
        }
        state<View.Success> {
            on<BackPressed> {
                sideEffect(SideEffect.Success)
            }
        }
        state<View.DisplayStoreMPDialog> {
            on<BackPressed> {
                sideEffect(SideEffect.Cancel)
            }
            on<DialogDismissed> {
                sideEffect(SideEffect.Cancel)
            }
            on<DialogConfirmed> {
                transitionTo(View.Loading(data))
            }
        }
        state<View.Loading> {
            on<BackPressed> {
                sideEffect(SideEffect.Cancel)
            }
            on<PinSaved> {
                transitionTo(View.Success(data), SideEffect.Success)
            }
        }
    }
}