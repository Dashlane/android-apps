package com.dashlane.accountsummary.changelogin.verification

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.dashlane.accountsummary.AccountSummaryDestination.ChangeLoginEmailVerification
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.BackPressed
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.HelpClicked
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.HelpDialogConfirmed
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.HelpDialogDismissed
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.NextClicked
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.Success
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.SuccessClicked
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.SuccessNewToken
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationEvent.TokenChanged
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.SideEffect
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View.DisplayHelpDialog
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View.DisplayToken
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View.Error
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View.Initial
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View.Loading
import com.dashlane.accountsummary.changelogin.verification.EmailVerificationState.View.LoadingNewToken
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, EmailVerificationEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle,
) : UDFViewModel<View, SideEffect, EmailVerificationEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        val newEmail = savedStateHandle.toRoute<ChangeLoginEmailVerification>().newEmail
        val initialState = Initial(EmailVerificationData(newEmail))
        start(createStateMachine(initialState))
    }

    @Suppress("LongMethod")
    internal fun createStateMachine(initialState: View) = StateMachine.create<View, EmailVerificationEvent, SideEffect> {
        initialState(initialState)
        state<Initial> {
            on<TokenChanged> {
                transitionTo(DisplayToken(data.copy(token = it.token, isContinueEnabled = it.token.text.isNotBlank(), error = null)))
            }
            on<HelpClicked> {
                transitionTo(DisplayHelpDialog(data.copy(showHelpDialog = true)))
            }
        }
        state<DisplayHelpDialog> {
            on<HelpDialogConfirmed> {
                transitionTo(LoadingNewToken(data.copy(showHelpDialog = false)))
            }
            on<HelpDialogDismissed> {
                transitionTo(DisplayToken(data.copy(showHelpDialog = false)))
            }
        }
        state<LoadingNewToken> {
            on<SuccessNewToken> {
                transitionTo(DisplayToken(data))
            }
        }
        state<DisplayToken> {
            on<TokenChanged> {
                transitionTo(DisplayToken(data.copy(token = it.token, isContinueEnabled = it.token.text.isNotBlank())))
            }
            on<HelpClicked> {
                transitionTo(DisplayHelpDialog(data.copy(showHelpDialog = true)))
            }
            on<NextClicked> {
                transitionTo(Loading(data.copy(isContinueEnabled = false)))
            }
        }
        state<Loading> {
            on<Success> {
                transitionTo(View.Success(data, isSessionValid = true))
            }
            on<EmailVerificationEvent.Error> {
                when (it.error) {
                    EmailVerificationError.InvalidSession -> transitionTo(View.Success(data, isSessionValid = false))
                    else -> transitionTo(Error(data.copy(error = it.error, isContinueEnabled = true)))
                }
            }
        }
        state<View.Success> {
            on<SuccessClicked> {
                sideEffect(SideEffect.Success(isSessionValid))
            }
            on<BackPressed> {
                sideEffect(SideEffect.Success(isSessionValid))
            }
        }
        state<Error> {
            on<TokenChanged> {
                transitionTo(DisplayToken(data.copy(token = it.token, isContinueEnabled = it.token.text.isNotBlank(), error = null)))
            }
            on<HelpClicked> {
                transitionTo(DisplayHelpDialog(data.copy(showHelpDialog = true)))
            }
            on<NextClicked> {
                transitionTo(Loading(data.copy(isContinueEnabled = false)))
            }
        }
    }
}