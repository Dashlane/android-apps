package com.dashlane.accountsummary.changecontact

import androidx.compose.ui.text.input.TextFieldValue
import com.dashlane.mvvm.State

data class ChangeContactEmailState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val currentContactEmail: String? = null,
    val newContactEmail: TextFieldValue? = null,
) : State.View

sealed class ChangeContactEmailNavState : State.SideEffect {
    data object Finish : ChangeContactEmailNavState()
}