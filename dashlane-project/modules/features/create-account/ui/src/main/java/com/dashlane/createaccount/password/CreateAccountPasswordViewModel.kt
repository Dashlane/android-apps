package com.dashlane.createaccount.password

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.dashlane.createaccount.CreateAccountDestination
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.BackPressed
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.BottomSheetDismissed
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.ConfirmPasswordChanged
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.FocusChanged
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.NextClicked
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.PasswordChanged
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.PasswordSaved
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.PasswordStrengthChanged
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.PasswordlessClicked
import com.dashlane.createaccount.password.CreateAccountPasswordEvent.TipsClicked
import com.dashlane.createaccount.password.CreateAccountPasswordState.SideEffect
import com.dashlane.createaccount.password.CreateAccountPasswordState.SideEffect.GoToPasswordless
import com.dashlane.createaccount.password.CreateAccountPasswordState.View
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.CheckPasswordStrength
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.DisplayPassword
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.DisplayPasswordConfirm
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.DisplayTips
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.Initial
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.SavePassword
import com.dashlane.createaccount.password.CreateAccountPasswordState.View.TransitionToConfirm
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class CreateAccountPasswordViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, CreateAccountPasswordEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : UDFViewModel<View, SideEffect, CreateAccountPasswordEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        val passwordRoute = savedStateHandle.toRoute<CreateAccountDestination.Password>()
        val initialState = Initial(CreateAccountPasswordData(passwordRoute.email, passwordRoute.isB2B))

        start(createStateMachine(initialState))
    }

    @Suppress("LongMethod")
    internal fun createStateMachine(initialState: View) = StateMachine.create<View, CreateAccountPasswordEvent, SideEffect> {
        initialState(initialState)
        state<Initial> {
            on<PasswordChanged> {
                transitionTo(CheckPasswordStrength(data.copy(password = it.password)))
            }
            on<BackPressed> {
                transitionTo(DisplayPassword(data.copy(password = TextFieldValue(""))), SideEffect.Cancel)
            }
            on<TipsClicked> {
                transitionTo(DisplayTips(data))
            }
            on<PasswordlessClicked> {
                sideEffect(GoToPasswordless(data.email))
            }
        }
        state<CheckPasswordStrength> {
            on<PasswordStrengthChanged> {
                transitionTo(
                    DisplayPassword(
                        data.copy(
                            passwordStrengthScore = it.passwordStrengthScore,
                            passwordStrength = it.passwordStrength,
                            isContinueEnabled = it.passwordStrengthScore >= PasswordStrengthScore.SAFELY_UNGUESSABLE,
                        ),
                    ),
                )
            }
        }
        state<DisplayPassword> {
            on<PasswordChanged> {
                transitionTo(CheckPasswordStrength(data.copy(password = it.password)))
            }
            on<BackPressed> {
                transitionTo(DisplayPassword(data.copy(password = TextFieldValue(""))), SideEffect.Cancel)
            }
            on<NextClicked> {
                val passwordStrengthScore = data.passwordStrengthScore
                    ?: throw IllegalStateException("passwordStrengthScore should not be null on NextClicked")
                if (passwordStrengthScore >= PasswordStrengthScore.SAFELY_UNGUESSABLE) {
                    transitionTo(TransitionToConfirm(data))
                } else {
                    transitionTo(DisplayPassword(data.copy(isContinueEnabled = false)))
                }
            }
            on<TipsClicked> {
                transitionTo(DisplayTips(data))
            }
            on<PasswordlessClicked> {
                sideEffect(GoToPasswordless(data.email))
            }
        }
        state<DisplayTips> {
            on<BottomSheetDismissed> {
                transitionTo(DisplayPassword(data))
            }
        }
        state<TransitionToConfirm> {
            on<ConfirmPasswordChanged> {
                val isMatching = it.confirmPassword.text == data.password.text
                transitionTo(
                    DisplayPasswordConfirm(
                        data.copy(confirmPassword = it.confirmPassword, isMatching = isMatching, isContinueEnabled = isMatching),
                    ),
                )
            }
        }
        state<DisplayPasswordConfirm> {
            on<BackPressed> {
                val isContinueEnabled = data.passwordStrengthScore?.let { it >= PasswordStrengthScore.SAFELY_UNGUESSABLE } == true
                transitionTo(DisplayPassword(data.copy(confirmPassword = TextFieldValue(""), isContinueEnabled = isContinueEnabled)))
            }
            on<ConfirmPasswordChanged> {
                val isMatching = it.confirmPassword.text == data.password.text
                transitionTo(
                    DisplayPasswordConfirm(
                        data.copy(confirmPassword = it.confirmPassword, isMatching = isMatching, isContinueEnabled = isMatching),
                    ),
                )
            }
            on<FocusChanged> {
                val isContinueEnabled = data.passwordStrengthScore?.let { it >= PasswordStrengthScore.SAFELY_UNGUESSABLE } == true
                transitionTo(DisplayPassword(data.copy(confirmPassword = TextFieldValue(""), isContinueEnabled = isContinueEnabled)))
            }
            on<NextClicked> {
                if (data.isMatching) {
                    transitionTo(SavePassword(data))
                } else {
                    noTransition()
                }
            }
            on<PasswordlessClicked> {
                sideEffect(GoToPasswordless(data.email))
            }
        }
        state<SavePassword> {
            on<PasswordSaved> {
                if (it.hasBiometrics) {
                    transitionTo(DisplayPasswordConfirm(data), SideEffect.GoToBiometricSetup)
                } else {
                    transitionTo(DisplayPasswordConfirm(data), SideEffect.GoToTermsAndConditions)
                }
            }
        }
    }
}