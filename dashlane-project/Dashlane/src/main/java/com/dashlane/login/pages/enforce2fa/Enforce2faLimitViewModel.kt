package com.dashlane.login.pages.enforce2fa

import com.dashlane.login.pages.enforce2fa.Enforce2faLimitState.SideEffect
import com.dashlane.login.pages.enforce2fa.Enforce2faLimitState.View
import com.dashlane.udf.StateMachine
import com.dashlane.udf.UDFViewModel
import com.dashlane.udf.UseCase
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class Enforce2faLimitViewModel @Inject constructor(
    useCases: MutableList<UseCase<View, Enforce2faLimitEvent>>,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : UDFViewModel<View, SideEffect, Enforce2faLimitEvent>(
        useCases = useCases,
        defaultDispatcher = defaultDispatcher,
    ) {
    init {
        start(createStateMachine())
    }

    internal fun createStateMachine() =
        StateMachine.create<View, Enforce2faLimitEvent, SideEffect> {
            initialState(View.Limit)
            state<View.Limit> {
                on<Enforce2faLimitEvent.ViewResumed> {
                    transitionTo(View.UpdateRedirectionFlag(redirectionDone = true))
                }
                on<Enforce2faLimitEvent.ViewPaused> {
                    transitionTo(View.UpdateRedirectionFlag(redirectionDone = false))
                }
                on<Enforce2faLimitEvent.LogoutClick> {
                    transitionTo(View.Logout)
                }
            }
            state<View.UpdateRedirectionFlag> {
                on<Enforce2faLimitEvent.RedirectionFlagUpdated> {
                    transitionTo(View.Limit)
                }
            }
            state<View.Logout> {
                on<Enforce2faLimitEvent.GoToLogin> {
                    transitionTo(View.Limit, sideEffect = SideEffect.GoToLogin)
                }
            }
        }
}