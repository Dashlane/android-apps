package com.dashlane.ui.activities.firstpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.csvimport.csvimport.ImportAuthentifiantHelper
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.isValidEmail
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getDefaultName
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFirstPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionManager: SessionManager,
    private val authentifiantHelper: ImportAuthentifiantHelper,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
    private val dataSaver: DataSaver,
    private val dataSync: DataSync,
) : ViewModel() {
    private val _stateFlow: MutableViewStateFlow<AddFirstPasswordState.View, AddFirstPasswordState.SideEffect>
    val stateFlow: ViewStateFlow<AddFirstPasswordState.View, AddFirstPasswordState.SideEffect>

    init {
        val url: String = savedStateHandle[AddFirstPasswordActivity.PARAM_URL]
            ?: throw IllegalStateException("No url provided to AddFirstPasswordActivity. Did you use its own new intent?")
        val initialState = AddFirstPasswordState.View(
            urlForThumbnail = url,
            email = sessionManager.session?.userId ?: "",
        )
        _stateFlow = MutableViewStateFlow(initialState)
        stateFlow = _stateFlow
    }

    fun onEmailChange(newEmail: String) {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                previousState.copy(
                    email = newEmail,
                    isSaveButtonEnabled = newEmail.isNotEmpty() && previousState.password.isNotEmpty(),
                )
            }
        }
    }

    fun onPasswordChange(newPassword: String) {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                previousState.copy(
                    password = newPassword,
                    isSaveButtonEnabled = previousState.email.isNotEmpty() && newPassword.isNotEmpty(),
                )
            }
        }
    }

    fun onHideOrRevealPasswordClick() {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                previousState.copy(
                    isPasswordObfuscated = !previousState.isPasswordObfuscated,
                )
            }
        }
    }

    fun onShowFAQClicked() {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                previousState.copy(showFAQ = true)
            }
        }
    }

    fun dismissFAQ() {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                previousState.copy(showFAQ = false)
            }
        }
    }

    fun onSaveCredentialClicked() {
        
        viewModelScope.launch {
            val currentState = stateFlow.viewState.value
            val credential = createCredential(
                url = currentState.urlForThumbnail,
                login = currentState.email,
                password = currentState.password,
            )
            if (dataSaver.save(credential)) {
                dataSync.sync(Trigger.SAVE)
                _stateFlow.update { previousState ->
                    previousState.copy(showAutofillDemoPrompt = true)
                }
            }
        }
    }

    private fun createCredential(
        url: String,
        login: String,
        password: String,
    ): VaultItem<SyncObject.Authentifiant> {
        val isValidEmail = login.isValidEmail()
        val teamSpaceAccessor = teamSpaceAccessorProvider.get()
        
        val teamId = if (teamSpaceAccessor?.canChangeTeamspace == true) {
            currentTeamSpaceUiFilter.currentFilter.teamSpace.teamId
        } else {
            null
        }
        return authentifiantHelper.newAuthentifiant(
            deprecatedUrl = url,
            email = login.takeIf { isValidEmail },
            login = login.takeUnless { isValidEmail },
            password = SyncObfuscatedValue(password),
            title = SyncObject.Authentifiant.getDefaultName(url),
            teamId = teamId,
        )
    }

    fun onTryAutofillDemoClicked() {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                previousState.copy(showAutofillDemoPrompt = false)
            }
            val currentState = stateFlow.viewState.value
            _stateFlow.send(
                AddFirstPasswordState.SideEffect.GoToAutofillDemo(
                    url = currentState.urlForThumbnail,
                    login = currentState.email,
                    password = currentState.password.toSyncObfuscatedValue(),
                ),
            )
        }
    }

    fun onDismissAutofillDemoClicked() {
        viewModelScope.launch {
            _stateFlow.update { previousState ->
                previousState.copy(showAutofillDemoPrompt = false)
            }
            _stateFlow.send(AddFirstPasswordState.SideEffect.GoToHomeOrChecklist)
        }
    }
}