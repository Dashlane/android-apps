package com.dashlane.autofill.generatepassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.autofill.createaccount.view.CreateAccountDialogFragmentArgs
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.useractivity.hermes.TrackingLogUtils
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneratePasswordViewModel @Inject constructor(
    private val service: AutofillGeneratePasswordService,
    userFeaturesChecker: UserFeaturesChecker,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stateFlow =
        MutableStateFlow<GeneratePasswordState>(GeneratePasswordState.Initial(GeneratePasswordData()))
    val uiState = stateFlow.asStateFlow()
    private var domainForLogs: Domain
    private var generateNewPasswordJob: Job? = null
    private var evaluatePasswordJob: Job? = null

    init {
        
        userFeaturesChecker.has(FeatureFlip.SPECIAL_PRIDE_MODE).let {
            if (it) {
                stateFlow.tryEmit(
                    GeneratePasswordState.Initial(stateFlow.value.data.copy(specialMode = GeneratePasswordSpecialMode.PRIDE))
                )
            }
        }

        
        
        val createAccountArgs = CreateAccountDialogFragmentArgs.fromSavedStateHandle(savedStateHandle)
        domainForLogs = TrackingLogUtils.createDomainForLog(
            createAccountArgs.argsWebpage,
            createAccountArgs.argsPackageName
        )
    }

    fun onGenerateButtonClicked(criteria: PasswordGeneratorCriteria) {
        generateNewPassword(criteria)
    }

    fun generateNewPassword(criteria: PasswordGeneratorCriteria) {
        generateNewPasswordJob?.cancel()
        generateNewPasswordJob = viewModelScope.launch {
            val result = service.generatePassword(criteria)
            stateFlow.emit(
                GeneratePasswordState.PasswordGenerated(
                    stateFlow.value.data.copy(lastGeneratedPassword = result.password),
                    result.password
                )
            )
        }
    }

    fun evaluatePassword(password: String) {
        evaluatePasswordJob?.cancel()
        evaluatePasswordJob = viewModelScope.launch {
            val strengthScore = if (password.isNotSemanticallyNull()) {
                service.evaluatePassword(password)
            } else {
                null
            }
            stateFlow.emit(GeneratePasswordState.Initial(stateFlow.value.data.copy(strengthScore = strengthScore)))
        }
    }

    fun onGeneratorConfigurationChanged(criteria: PasswordGeneratorCriteria) {
        generateNewPassword(criteria)
        service.setPasswordGeneratorDefaultCriteria(criteria)
    }

    fun saveGeneratedPasswordIfUsed(result: VaultItem<SyncObject.Authentifiant>) {
        viewModelScope.launch {
            val authentifiant = result.syncObject
            val password = authentifiant.password.toString()
            if (authentifiant.password?.equalsString(uiState.value.data.lastGeneratedPassword) == true) {
                authentifiant.urlForGoToWebsite?.toUrlOrNull()?.root?.let { domain ->
                    service.saveToPasswordHistory(password, domain, result.uid)
                }
            }
            stateFlow.emit(GeneratePasswordState.PasswordSavedToHistory(stateFlow.value.data, result))
        }
    }
}
