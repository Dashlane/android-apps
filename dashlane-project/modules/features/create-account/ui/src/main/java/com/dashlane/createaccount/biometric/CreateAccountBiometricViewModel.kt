package com.dashlane.createaccount.biometric

import com.dashlane.createaccount.biometric.CreateAccountBiometricEvent.BiometricCheckChanged
import com.dashlane.createaccount.biometric.CreateAccountBiometricEvent.BiometricRecoveryCheckChanged
import com.dashlane.createaccount.biometric.CreateAccountBiometricEvent.HelpClicked
import com.dashlane.createaccount.biometric.CreateAccountBiometricEvent.NextClicked
import com.dashlane.createaccount.biometric.CreateAccountBiometricEvent.Success
import com.dashlane.createaccount.biometric.CreateAccountBiometricState.SideEffect
import com.dashlane.createaccount.biometric.CreateAccountBiometricState.View
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class CreateAccountBiometricViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, CreateAccountBiometricEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, CreateAccountBiometricEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    internal val stateMachine = StateMachine.create<View, CreateAccountBiometricEvent, SideEffect> {
        initialState(View.Initial(CreateAccountBiometricData()))
        state<View.Initial> {
            on<BiometricCheckChanged> {
                transitionTo(
                    View.Initial(
                        data.copy(
                            biometricChecked = it.isChecked,
                            biometricRecoveryChecked = false,
                            snackBarShown = !it.isChecked && data.biometricRecoveryChecked,
                        ),
                    ),
                )
            }
            on<BiometricRecoveryCheckChanged> {
                transitionTo(
                    View.Initial(
                        data.copy(
                            biometricChecked = data.biometricChecked || it.isChecked,
                            biometricRecoveryChecked = it.isChecked,
                            snackBarShown = it.isChecked && !data.biometricChecked,
                        ),
                    ),
                )
            }
            on<HelpClicked> {
                transitionTo(View.Initial(data.copy(helpShown = !data.helpShown)))
            }
            on<NextClicked> {
                transitionTo(View.Loading(data))
            }
        }
        state<View.Loading> {
            on<Success> {
                transitionTo(View.Initial(data), SideEffect.Success)
            }
        }
    }

    init {
        start(stateMachine)
    }
}