package com.dashlane.accountsummary.changelogin

import com.dashlane.accountsummary.changelogin.ChangeLoginEmailEvent.Cancel
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailEvent.CurrentEmailLoaded
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailEvent.EmailValid
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailEvent.NewEmailChanged
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailEvent.NextClicked
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailState.SideEffect
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailState.SideEffect.Finish
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailState.View
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailState.View.DisplayEmail
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailState.View.DisplayNewEmail
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailState.View.Error
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailState.View.Initial
import com.dashlane.accountsummary.changelogin.ChangeLoginEmailState.View.Loading
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.util.isValidEmail
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class ChangeLoginEmailViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, ChangeLoginEmailEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, ChangeLoginEmailEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        start(createStateMachine())
    }

    @Suppress("LongMethod")
    internal fun createStateMachine() = StateMachine.create<View, ChangeLoginEmailEvent, SideEffect> {
        initialState(Initial(ChangeLoginEmailData()))
        state<Initial> {
            on<CurrentEmailLoaded> {
                transitionTo(DisplayEmail(data.copy(currentLoginEmail = it.email)))
            }
            on<Cancel> {
                sideEffect(Finish)
            }
        }
        state<DisplayEmail> {
            on<NewEmailChanged> {
                transitionTo(
                    DisplayNewEmail(
                        data.copy(newLoginEmail = it.newEmail, isContinueEnabled = it.newEmail.text.isValidEmail(), error = null),
                    ),
                )
            }
        }
        state<DisplayNewEmail> {
            on<NewEmailChanged> {
                transitionTo(
                    DisplayNewEmail(
                        data.copy(newLoginEmail = it.newEmail, isContinueEnabled = it.newEmail.text.isValidEmail(), error = null),
                    ),
                )
            }
            on<NextClicked> {
                transitionTo(Loading(data.copy(isContinueEnabled = false)))
            }
        }
        state<Loading> {
            on<EmailValid> {
                transitionTo(DisplayNewEmail(data.copy(isContinueEnabled = true)), SideEffect.Next(newEmail = it.newEmail))
            }
            on<ChangeLoginEmailEvent.Error> {
                transitionTo(Error(data.copy(isContinueEnabled = data.newLoginEmail?.text.isValidEmail(), error = it.error)))
            }
        }
        state<Error> {
            on<NewEmailChanged> {
                transitionTo(
                    DisplayNewEmail(
                        data.copy(newLoginEmail = it.newEmail, isContinueEnabled = it.newEmail.text.isValidEmail(), error = null),
                    ),
                )
            }
            on<NextClicked> {
                transitionTo(Loading(data.copy(isContinueEnabled = false)))
            }
        }
    }
}