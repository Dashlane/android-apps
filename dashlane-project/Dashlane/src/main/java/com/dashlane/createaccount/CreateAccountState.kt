package com.dashlane.createaccount

import com.dashlane.mvvm.State

sealed class CreateAccountState : State {
    data class View(
        val prefilledEmail: String? = null,
        val startDestination: CreateAccountDestination,
    ) : CreateAccountState(), State.View

    sealed class SideEffect : CreateAccountState(), State.SideEffect
}