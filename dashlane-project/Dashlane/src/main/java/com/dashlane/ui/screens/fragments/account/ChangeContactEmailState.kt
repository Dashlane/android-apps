package com.dashlane.ui.screens.fragments.account

import com.dashlane.mvvm.State

data class ChangeContactEmailState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val currentContactEmail: String? = null,
    val newContactEmail: String? = null
) : State.View

sealed class ChangeContactEmailNavState : State.SideEffect {
    data object Finish : ChangeContactEmailNavState()
}