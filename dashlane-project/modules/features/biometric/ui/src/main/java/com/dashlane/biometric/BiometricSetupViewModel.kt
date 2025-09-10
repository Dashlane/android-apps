package com.dashlane.biometric

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.SavedStateHandle
import com.dashlane.biometric.BiometricSetupEvent.AuthenticationError
import com.dashlane.biometric.BiometricSetupEvent.AuthenticationFailed
import com.dashlane.biometric.BiometricSetupEvent.AuthenticationSuccess
import com.dashlane.biometric.BiometricSetupEvent.BiometricChecked
import com.dashlane.biometric.BiometricSetupEvent.BiometricRecoveryChecked
import com.dashlane.biometric.BiometricSetupEvent.BiometricSetupPositive
import com.dashlane.biometric.BiometricSetupEvent.Cancel
import com.dashlane.biometric.BiometricSetupEvent.DialogDismissed
import com.dashlane.biometric.BiometricSetupEvent.DialogPositive
import com.dashlane.biometric.BiometricSetupEvent.Error
import com.dashlane.biometric.BiometricSetupEvent.RecoverySetupPositive
import com.dashlane.biometric.BiometricSetupEvent.Success
import com.dashlane.biometric.BiometricSetupEvent.ViewResumed
import com.dashlane.biometric.BiometricSetupState.SideEffect
import com.dashlane.biometric.BiometricSetupState.View
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class BiometricSetupViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, BiometricSetupEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : UDFViewModel<View, SideEffect, BiometricSetupEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        val isBiometricRecovery = savedStateHandle.get<Boolean>(BiometricSetupActivity.IS_BIOMETRIC_RECOVERY) == true
        val initialState = View.Initial(BiometricSetupData(isBiometricRecovery = isBiometricRecovery))
        start(createStateMachine(initialState))
    }

    @Suppress("LongMethod", "kotlin:S3776")
    internal fun createStateMachine(initialState: View) = StateMachine.create<View, BiometricSetupEvent, SideEffect> {
        initialState(initialState)
        state<View.Initial> {
            on<ViewResumed> {
                if (data.isBiometricRecovery) {
                    transitionTo(View.LoadingRecovery(data))
                } else {
                    transitionTo(View.Loading(data))
                }
            }
        }
        state<View.Loading> {
            on<BiometricChecked> {
                transitionTo(
                    View.DisplayBiometricPrompt(
                        data.copy(
                            authenticator = it.authenticator,
                            showBiometricPrompt = true,
                            cryptoObject = it.cryptoObject,
                        ),
                    ),
                )
            }
            on<Error> {
                transitionTo(View.Error(data.copy(error = it.error)))
            }
            on<Cancel> {
                sideEffect(SideEffect.Cancel)
            }
        }
        state<View.LoadingRecovery> {
            on<BiometricRecoveryChecked> {
                transitionTo(View.DisplayBottomSheet(data.copy(showBiometricSetupBottomSheet = true)))
            }
            on<Success> {
                sideEffect(SideEffect.Success)
            }
            on<Cancel> {
                sideEffect(SideEffect.Cancel)
            }
        }
        state<View.DisplayBiometricPrompt> {
            on<Cancel> {
                transitionTo(View.Initial(data.copy(showBiometricPrompt = false, cryptoObject = null)), SideEffect.Cancel)
            }
            on<AuthenticationSuccess> {
                transitionTo(View.SaveBiometric(data))
            }
            on<AuthenticationError> {
                when (it.code) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                    BiometricPrompt.ERROR_TIMEOUT,
                    BiometricPrompt.ERROR_USER_CANCELED,
                    -> transitionTo(
                        state = View.Initial(data.copy(showBiometricPrompt = false, cryptoObject = null)),
                        sideEffect = SideEffect.Cancel,
                    )
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> transitionTo(
                        state = View.Initial(data.copy(showBiometricPrompt = false, cryptoObject = null)),
                        sideEffect = SideEffect.GoToSettings,
                    )
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
                    -> transitionTo(
                        state = View.Initial(data.copy(showBiometricPrompt = false, cryptoObject = null)),
                        sideEffect = SideEffect.CancelWithToast(it.code, it.message),
                    )
                    else -> transitionTo(
                        View.Initial(data.copy(showBiometricPrompt = false, cryptoObject = null)),
                        SideEffect.CancelWithToast(code = null, message = null),
                    )
                }
            }
            on<AuthenticationFailed> {
                noTransition()
            }
        }
        state<View.DisplayBottomSheet> {
            on<BiometricSetupPositive> {
                transitionTo(View.Loading(data))
            }
            on<RecoverySetupPositive> {
                transitionTo(View.SaveBiometricRecovery(data), SideEffect.Success)
            }
            on<DialogDismissed> {
                sideEffect(SideEffect.Cancel)
            }
        }
        state<View.SaveBiometric> {
            on<Cancel> {
                sideEffect(SideEffect.Cancel)
            }
            on<Success> {
                if (it.goToRecovery) {
                    transitionTo(View.DisplayBottomSheet(data.copy(showBiometricRecoverySetupBottomSheet = true)))
                } else {
                    sideEffect(SideEffect.Success)
                }
            }
        }
        state<View.Error> {
            on<ViewResumed> {
                transitionTo(View.Loading(data.copy(error = null)))
            }
            on<Cancel> {
                sideEffect(SideEffect.Cancel)
            }
            on<DialogPositive> {
                when (it.error) {
                    BiometricSetupError.BiometricNotRegistered,
                    BiometricSetupError.RequireStrongerBiometric,
                    -> sideEffect(SideEffect.GoToSettings)
                }
            }
            on<DialogDismissed> {
                sideEffect(SideEffect.Cancel)
            }
        }
    }
}