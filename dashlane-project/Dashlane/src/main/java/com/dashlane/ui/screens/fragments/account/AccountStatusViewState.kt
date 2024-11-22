package com.dashlane.ui.screens.fragments.account

import com.dashlane.mvvm.State

data class AccountStatusViewState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val loginEmail: String? = null,
    val contactEmail: String? = null
) : State.View

sealed class AccountStatusNavState : State.SideEffect {
    data class EditContactForm(val contactEmail: String?) : AccountStatusNavState()
}
