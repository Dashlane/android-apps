package com.dashlane.createaccount

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.dashlane.createaccount.CreateAccountComposeActivity.Companion.EXTRA_PRE_FILLED_EMAIL
import com.dashlane.createaccount.CreateAccountComposeActivity.Companion.EXTRA_SKIP_EMAIL_IF_PRE_FILLED
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _stateFlow: MutableViewStateFlow<CreateAccountState.View, CreateAccountState.SideEffect>
    val stateFlow: ViewStateFlow<CreateAccountState.View, CreateAccountState.SideEffect>

    init {
        val prefilledEmail: String? = savedStateHandle[EXTRA_PRE_FILLED_EMAIL]
        val skipEmailIfPrefilled: Boolean = savedStateHandle[EXTRA_SKIP_EMAIL_IF_PRE_FILLED] ?: false
        val startDestination = CreateAccountDestination.Email(prefilledEmail, skipEmailIfPrefilled)

        _stateFlow = MutableViewStateFlow(
            CreateAccountState.View(
                prefilledEmail = prefilledEmail,
                startDestination = startDestination,
            ),
        )
        stateFlow = _stateFlow
    }
}