package com.dashlane.accountsummary

import com.dashlane.mvvm.State

data class AccountStatusViewState(
    val isLoading: Boolean = true,
    val canChangeLoginEmail: Boolean = false,
    val isError: Boolean = false,
    val loginEmail: String? = null,
    val contactEmail: String? = null,
) : State.View

sealed class AccountSummaryNavState : State.SideEffect {
    data object LockPrompt : AccountSummaryNavState()

    data class EditLoginForm(val loginEmail: String?) : AccountSummaryNavState()

    data class EditContactForm(val contactEmail: String?) : AccountSummaryNavState()
}