package com.dashlane.createaccount.email

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.dashlane.createaccount.CreateAccountDestination.Email
import com.dashlane.createaccount.email.CreateAccountEmailEvent.AccountIsSso
import com.dashlane.createaccount.email.CreateAccountEmailEvent.CancelDialog
import com.dashlane.createaccount.email.CreateAccountEmailEvent.ConfirmEmail
import com.dashlane.createaccount.email.CreateAccountEmailEvent.ContinueClicked
import com.dashlane.createaccount.email.CreateAccountEmailEvent.EmailChanged
import com.dashlane.createaccount.email.CreateAccountEmailEvent.EmailUnlikelyValid
import com.dashlane.createaccount.email.CreateAccountEmailEvent.EmailValid
import com.dashlane.createaccount.email.CreateAccountEmailEvent.Error
import com.dashlane.createaccount.email.CreateAccountEmailEvent.HelpClicked
import com.dashlane.createaccount.email.CreateAccountEmailEvent.SsoLoginComplete
import com.dashlane.createaccount.email.CreateAccountEmailState.SideEffect
import com.dashlane.createaccount.email.CreateAccountEmailState.SideEffect.GoToHelp
import com.dashlane.createaccount.email.CreateAccountEmailState.SideEffect.GoToNext
import com.dashlane.createaccount.email.CreateAccountEmailState.SideEffect.GoToSso
import com.dashlane.createaccount.email.CreateAccountEmailState.View
import com.dashlane.createaccount.email.CreateAccountEmailState.View.DisplayEmail
import com.dashlane.createaccount.email.CreateAccountEmailState.View.DisplayExpiredVersionErrorDialog
import com.dashlane.createaccount.email.CreateAccountEmailState.View.DisplaySsoAdminErrorDialog
import com.dashlane.createaccount.email.CreateAccountEmailState.View.DisplayValidateEmailDialog
import com.dashlane.createaccount.email.CreateAccountEmailState.View.Initial
import com.dashlane.createaccount.email.CreateAccountEmailState.View.Loading
import com.dashlane.debug.services.DaDaDaLogin
import com.dashlane.sso.login.RemoteLoginSsoActivity
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isValidEmail
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class CreateAccountEmailViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, CreateAccountEmailEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
    daDaDaLogin: DaDaDaLogin,
    savedStateHandle: SavedStateHandle,
) : UDFViewModel<View, SideEffect, CreateAccountEmailEvent>(useCases = useCases, defaultDispatcher = defaultDispatcher) {
    init {
        val emailRoute = savedStateHandle.toRoute<Email>()
        val email: String? = if (emailRoute.email.isNullOrBlank() && daDaDaLogin.isEnabled) {
            daDaDaLogin.generateUsername()
        } else {
            emailRoute.email
        }

        val initialState = if (email.isNotSemanticallyNull()) {
            val emailTextFieldValue = email?.let { TextFieldValue(it, selection = TextRange(it.length)) } ?: TextFieldValue("")
            val isContinueEnabled = emailTextFieldValue.text.isValidEmail()
            DisplayEmail(CreateAccountEmailData(email = emailTextFieldValue, isContinueEnabled = isContinueEnabled))
        } else {
            Initial(CreateAccountEmailData())
        }

        start(createStateMachine(initialState))

        if (email.isNotSemanticallyNull() && emailRoute.skipIfPrefilled) {
            sendEvent(ContinueClicked)
        }
    }

    @Suppress("LongMethod")
    internal fun createStateMachine(initialState: View) = StateMachine.create<View, CreateAccountEmailEvent, SideEffect> {
        initialState(initialState)
        state<Initial> {
            on<EmailChanged> {
                transitionTo(DisplayEmail(data.copy(email = it.email, isContinueEnabled = it.email.text.isValidEmail(), error = null)))
            }
        }
        state<DisplayEmail> {
            on<EmailChanged> {
                transitionTo(DisplayEmail(data.copy(email = it.email, isContinueEnabled = it.email.text.isValidEmail(), error = null)))
            }
            on<ContinueClicked> {
                transitionTo(Loading(data))
            }
            on<SsoLoginComplete> {
                when (it.result) {
                    RemoteLoginSsoActivity.Result.Success -> sideEffect()
                    is RemoteLoginSsoActivity.Result.Error -> transitionTo(DisplayEmail(data.copy(error = CreateAccountEmailError.Sso)))
                }
            }
        }
        state<Loading> {
            on<EmailUnlikelyValid> {
                transitionTo(DisplayValidateEmailDialog(data.copy(result = it.result)))
            }
            on<EmailValid> {
                transitionTo(DisplayEmail(data.copy(result = null)), GoToNext(it.email, it.isB2B))
            }
            on<AccountIsSso> {
                transitionTo(DisplayEmail(data), GoToSso(it.ssoInfo))
            }
            on<Error> {
                when (it.error) {
                    CreateAccountEmailError.AdminSso -> transitionTo(DisplaySsoAdminErrorDialog(data))
                    CreateAccountEmailError.AuthenticationExpiredVersion -> transitionTo(DisplayExpiredVersionErrorDialog(data))
                    else -> transitionTo(View.Error(data.copy(result = null, error = it.error)))
                }
            }
        }
        state<DisplayValidateEmailDialog> {
            on<CancelDialog> {
                transitionTo(DisplayEmail(data.copy(result = null)))
            }
            on<ConfirmEmail> {
                transitionTo(Loading(data))
            }
        }
        state<DisplaySsoAdminErrorDialog> {
            on<CancelDialog> {
                transitionTo(DisplayEmail(data))
            }
        }
        state<DisplayExpiredVersionErrorDialog> {
            on<HelpClicked> {
                transitionTo(DisplayEmail(data.copy(result = null)), GoToHelp)
            }
            on<CancelDialog> {
                transitionTo(DisplayEmail(data))
            }
        }
        state<View.Error> {
            on<EmailChanged> {
                transitionTo(DisplayEmail(data.copy(email = it.email, isContinueEnabled = it.email.text.isValidEmail(), error = null)))
            }
            on<ContinueClicked> {
                transitionTo(Loading(data.copy(error = null)))
            }
        }
    }
}