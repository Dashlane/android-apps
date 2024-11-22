package com.dashlane.ui.screens.fragments.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.server.api.endpoints.account.UpdateContactInfoService
import com.dashlane.session.SessionManager
import com.dashlane.session.authorization
import com.dashlane.session.repository.UserAccountInfoRepository
import com.dashlane.util.isValidEmail
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeContactEmailViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userAccountInfoRepository: UserAccountInfoRepository,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val updateContactInfoService: UpdateContactInfoService
) : ViewModel() {
    private val _uiState =
        MutableViewStateFlow<ChangeContactEmailState, ChangeContactEmailNavState>(ChangeContactEmailState())
    val uiState: ViewStateFlow<ChangeContactEmailState, ChangeContactEmailNavState> = _uiState

    init {
        viewModelScope.launch(defaultDispatcher) {
            loadEmails()
        }
    }

    fun onNewContactEmailChange(newContactEmail: String) {
        _uiState.update {
            it.copy(newContactEmail = newContactEmail, isSaveEnabled = newContactEmail.isValidEmail(), isError = false)
        }
    }

    fun onSendNewContactEmailClicked(newContactEmail: String?) {
        newContactEmail ?: return
        _uiState.update {
            it.copy(isSaveEnabled = false, isError = false)
        }
        viewModelScope.launch {
            val session = sessionManager.session
            if (session == null) {
                _uiState.update {
                    it.copy(isError = true, isSaveEnabled = it.newContactEmail.isValidEmail())
                }
                return@launch
            }
            runCatching {
                updateContactInfoService.execute(
                    session.authorization,
                    UpdateContactInfoService.Request(
                        contactEmail = UpdateContactInfoService.Request.ContactEmail(newContactEmail)
                    )
                )
            }.onSuccess {
                userAccountInfoRepository.refreshUserAccountInfo(session = session)
                _uiState.send(ChangeContactEmailNavState.Finish)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaveEnabled = it.newContactEmail.isValidEmail(), isError = true)
                }
            }
        }
    }

    private fun loadEmails() {
        viewModelScope.launch {
            val session = sessionManager.session
            if (session == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        currentContactEmail = null,
                        newContactEmail = null
                    )
                }
                return@launch
            }
            val userAccountInfo = userAccountInfoRepository[session]
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isError = false,
                    currentContactEmail = userAccountInfo?.contactEmail ?: session.username.email,
                    newContactEmail = null
                )
            }
        }
    }
}