package com.dashlane.autofill.changepassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.changepassword.domain.AutofillChangePasswordErrors
import com.dashlane.autofill.changepassword.domain.CredentialUpdateInfo
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.loginForUi
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    @IoCoroutineDispatcher private val ioCoroutineDispatcher: CoroutineDispatcher,
    private val logger: AutofillChangePasswordLogger,
    private val provider: ChangePasswordDataProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val stateFlow = MutableStateFlow<AutofillChangePasswordState>(
        AutofillChangePasswordState.Initial(AutofillChangePasswordData(false))
    )
    val uiState = stateFlow.asStateFlow()

    private val navArgs = ChangePasswordDialogFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val packageName: String? = navArgs.argsPackageName
    private val website: String? = navArgs.argsWebpage

    private var defaultCredentialId: String = ""
    private var websiteCredentials = emptyList<VaultItem<SyncObject.Authentifiant>>()

    init {
        logger.packageName = packageName
        logger.domain = website
    }

    fun useNewPassword(login: String, password: String?) {
        viewModelScope.launch {
            if (login.isSemanticallyNull() || password.isSemanticallyNull()) {
                handleError(AutofillChangePasswordErrors.INCOMPLETE)
                return@launch
            }
            stateFlow.emit(AutofillChangePasswordState.Initial(stateFlow.value.data.copy(canUse = false)))
            val item = provider.getCredential(login)
            val result = withContext(ioCoroutineDispatcher) {
                val info = CredentialUpdateInfo(item.uid, password!!)
                provider.updateCredentialToVault(info)
            }
            stateFlow.emit(AutofillChangePasswordState.Initial(stateFlow.value.data.copy(canUse = true)))
            if (result == null) {
                handleError(AutofillChangePasswordErrors.DATABASE_ERROR)
                return@launch
            }

            logger.logUpdate(result.uid)
            stateFlow.emit(AutofillChangePasswordState.PasswordChanged(stateFlow.value.data, result, item))
        }
    }

    fun onCancel() {
        stateFlow.tryEmit(AutofillChangePasswordState.Cancelled(stateFlow.value.data))
        logger.logCancel(defaultCredentialId)
    }

    fun prefillLogin() {
        viewModelScope.launch {
            websiteCredentials = provider.loadAuthentifiants(website, packageName)
            val logins = websiteCredentials.mapNotNull { it.loginForUi }
            if (logins.isEmpty()) {
                handleError(AutofillChangePasswordErrors.NO_MATCHING_CREDENTIAL)
                return@launch
            }
            stateFlow.emit(AutofillChangePasswordState.PrefillLogin(stateFlow.value.data, logins))
        }
    }

    fun setDefaultCredential(filledLogin: String?) {
        defaultCredentialId = websiteCredentials.first {
            val syncObject = it.syncObject
            syncObject.login == filledLogin || syncObject.email == filledLogin
        }.uid
        logger.logOnClickUpdateAccount(defaultCredentialId)
    }

    fun updateCanUse(login: String?, password: String?) {
        if (login.isNotSemanticallyNull() && password.isNotSemanticallyNull()) {
            stateFlow.tryEmit(AutofillChangePasswordState.Initial(stateFlow.value.data.copy(canUse = true)))
        } else {
            stateFlow.tryEmit(AutofillChangePasswordState.Initial(stateFlow.value.data.copy(canUse = false)))
        }
    }

    private fun handleError(errorRaised: AutofillChangePasswordErrors) {
        stateFlow.tryEmit(
            AutofillChangePasswordState.Error(
                stateFlow.value.data,
                error = errorRaised
            )
        )
    }
}
