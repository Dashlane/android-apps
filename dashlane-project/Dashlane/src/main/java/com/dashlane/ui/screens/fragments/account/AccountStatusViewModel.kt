package com.dashlane.ui.screens.fragments.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserAccountInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountStatusViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userAccountInfoRepository: UserAccountInfoRepository
) : ViewModel() {
    private val _uiState = MutableViewStateFlow<AccountStatusViewState, AccountStatusNavState>(AccountStatusViewState())
    val uiState: ViewStateFlow<AccountStatusViewState, AccountStatusNavState> = _uiState

    fun loadEmails() {
        viewModelScope.launch {
            val session = sessionManager.session
            val userAccountInfo = userAccountInfoRepository[session]
            if (session != null && userAccountInfo != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = false,
                        loginEmail = session.username.email,
                        contactEmail = userAccountInfo.contactEmail ?: session.username.email
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true
                    )
                }
            }
        }
    }

    fun onClickEditContactEmail(contactEmail: String?) {
        viewModelScope.launch {
            _uiState.send(AccountStatusNavState.EditContactForm(contactEmail))
        }
    }
}