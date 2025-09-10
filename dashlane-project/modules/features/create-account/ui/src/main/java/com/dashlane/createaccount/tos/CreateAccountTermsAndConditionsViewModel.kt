package com.dashlane.createaccount.tos

import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsEvent.AccountCreated
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsEvent.HelpClicked
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsEvent.NextClicked
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsEvent.OffersCheckChanged
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsEvent.TosCheckChanged
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsState.SideEffect
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsState.View
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsState.View.Initial
import com.dashlane.createaccount.tos.CreateAccountTermsAndConditionsState.View.Loading
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class CreateAccountTermsAndConditionsViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, CreateAccountTermsAndConditionsEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, CreateAccountTermsAndConditionsEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    internal val stateMachine = StateMachine.create<View, CreateAccountTermsAndConditionsEvent, SideEffect> {
        initialState(Initial(CreateAccountTermsAndConditionsData()))
        state<Initial> {
            on<TosCheckChanged> {
                transitionTo(Initial(data.copy(isTosChecked = it.isChecked, isContinueEnabled = it.isChecked)))
            }
            on<OffersCheckChanged> {
                transitionTo(Initial(data.copy(isOffersChecked = it.isChecked)))
            }
            on<HelpClicked> {
                sideEffect(SideEffect.GoToHelp(it.uri))
            }
            on<NextClicked> {
                transitionTo(Loading(data))
            }
        }
        state<Loading> {
            on<AccountCreated> {
                transitionTo(Initial(data), SideEffect.Success)
            }
        }
    }

    init {
        start(stateMachine)
    }
}