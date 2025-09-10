
package com.dashlane.accountsummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockManager
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.session.SessionManager
import com.dashlane.storage.securestorage.UserSecureStorageManager
import com.dashlane.user.UserAccountInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSummaryViewModel @Inject constructor(
    private val lockManager: LockManager,
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val accountStatusRepository: AccountStatusRepository,
    private val userSecureStorageManager: UserSecureStorageManager,
    private val userFeaturesChecker: UserFeaturesChecker,
    private val logRepository: LogRepository,
) : ViewModel() {
    private val _uiState = MutableViewStateFlow<AccountStatusViewState, AccountSummaryNavState>(AccountStatusViewState())
    val uiState: ViewStateFlow<AccountStatusViewState, AccountSummaryNavState> = _uiState

    init {
        logRepository.queuePageView(component = BrowseComponent.MAIN_APP, page = AnyPage.SETTINGS_ACCOUNT)
    }

    fun loadEmails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            sessionManager.session?.let { session ->
                val username = session.username
                val isMasterPassword = userAccountStorage[username]?.accountType is UserAccountInfo.AccountType.MasterPassword
                val isB2b = accountStatusRepository[session]?.premiumStatus?.b2bStatus?.currentTeam != null
                val canChangeLoginEmail = userFeaturesChecker.has(FeatureFlip.CHANGE_LOGIN_EMAIL) && isMasterPassword && !isB2b

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        canChangeLoginEmail = canChangeLoginEmail,
                        isError = false,
                        loginEmail = username.email,
                        contactEmail = userSecureStorageManager.readUserContactEmail(session.localKey, username) ?: username.email,
                    )
                }
            } ?: run {
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

    fun onClickEditLoginEmail() {
        viewModelScope.launch {
            _uiState.send(AccountSummaryNavState.LockPrompt)
            val lockEvent = lockManager.lockEventFlow.first()
            if (lockEvent is LockEvent.Unlock) {
                _uiState.send(AccountSummaryNavState.EditLoginForm(_uiState.value.contactEmail))
            }
        }
    }

    fun onClickEditContactEmail() {
        viewModelScope.launch {
            _uiState.send(AccountSummaryNavState.LockPrompt)
            val lockEvent = lockManager.lockEventFlow.first()
            if (lockEvent is LockEvent.Unlock) {
                _uiState.send(AccountSummaryNavState.EditContactForm(_uiState.value.contactEmail))
            }
        }
    }
}