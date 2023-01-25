package com.dashlane.authenticator.suggestions

import com.dashlane.authenticator.AuthenticatorBaseViewModelContract
import kotlinx.coroutines.flow.Flow

interface AuthenticatorSuggestionsViewModelContract : AuthenticatorBaseViewModelContract {
    val uiState: Flow<AuthenticatorSuggestionsUiState>
}