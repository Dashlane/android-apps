package com.dashlane.autofill.createaccount.view

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.autofill.createaccount.AutofillCreateAccountLogger
import com.dashlane.autofill.createaccount.CreateAccountDataProvider
import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountErrors
import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountService
import com.dashlane.autofill.createaccount.domain.CredentialInfo
import com.dashlane.autofill.util.AutofillLogUtil
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    @field:SuppressLint("StaticFieldLeak") @ApplicationContext private val context: Context,
    @IoCoroutineDispatcher private val ioCoroutineDispatcher: CoroutineDispatcher,
    private val service: AutofillCreateAccountService,
    private val provider: CreateAccountDataProvider,
    private val logger: AutofillCreateAccountLogger,
    private val teamSpaceRepository: TeamSpaceAccessorProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stateFlow = MutableStateFlow<AutofillCreateAccountState>(
        AutofillCreateAccountState.Initial(AutofillCreateAccountData(false, listOf()))
    )
    val uiState = stateFlow.asStateFlow()

    private val navArgs = CreateAccountDialogFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val packageName: String? = navArgs.argsPackageName
    private val website: String? = navArgs.argsWebpage
    private val teamspaceAccessor: TeamSpaceAccessor?
        get() = teamSpaceRepository.get()

    fun initData() {
        val emailList = service.loadExistingLogins()
        val websiteList = service.getFamousWebsitesList()
        val teamSpaces = if (teamspaceAccessor?.canChangeTeamspace == true) {
            teamspaceAccessor?.availableSpaces?.filter { it !is TeamSpace.Combined }
        } else {
            null
        }
        stateFlow.tryEmit(
            AutofillCreateAccountState.InitSuggestions(
                stateFlow.value.data.copy(teamSpace = teamSpaces),
                emailList,
                websiteList
            )
        )
    }

    fun save(userInputWebsite: String?, login: String?, password: String?, spaceSelectedIndex: Int) {
        viewModelScope.launch {
            if (login.isSemanticallyNull() || password.isSemanticallyNull()) {
                handleError(AutofillCreateAccountErrors.INCOMPLETE)
                return@launch
            }

            val result = withContext(ioCoroutineDispatcher) {
                stateFlow.emit(AutofillCreateAccountState.Initial(stateFlow.value.data.copy(canSave = false)))

                val info = CredentialInfo(
                    title = provider.getCredentialTitle(context, website, packageName, userInputWebsite),
                    website = userInputWebsite,
                    login = login!!,
                    password = password!!,
                    packageName = packageName,
                    spaceId = (stateFlow.value.data.teamSpace?.getOrNull(spaceSelectedIndex) as? TeamSpace.Business)?.teamId
                )
                provider.saveCredentialToVault(info)
            }
            stateFlow.emit(AutofillCreateAccountState.Initial(stateFlow.value.data.copy(canSave = true)))
            if (result == null) {
                handleError(AutofillCreateAccountErrors.DATABASE_ERROR)
                return@launch
            }

            logger.logSave(
                domainWrapper = AutofillLogUtil.extractDomainFrom(
                    urlDomain = userInputWebsite?.toUrlDomainOrNull(),
                    packageName = packageName
                ),
                credential = result
            )
            stateFlow.emit(
                AutofillCreateAccountState.AccountCreated(stateFlow.value.data, result)
            )
        }
    }

    private fun handleError(errorRaised: AutofillCreateAccountErrors) {
        stateFlow.tryEmit(
            AutofillCreateAccountState.Error(
                stateFlow.value.data,
                error = errorRaised
            )
        )
    }

    fun updateCanSave(login: String?, password: String?) {
        if (login.isNotSemanticallyNull() && password.isNotSemanticallyNull()) {
            stateFlow.tryEmit(AutofillCreateAccountState.Initial(stateFlow.value.data.copy(canSave = true)))
        } else {
            stateFlow.tryEmit(AutofillCreateAccountState.Initial(stateFlow.value.data.copy(canSave = false)))
        }
    }

    fun getContentForWebsiteField(): String? {
        return when {
            website.isNotSemanticallyNull() -> website
            packageName != null -> provider.getMatchingWebsite(packageName)
            else -> {
                null
            }
        }
    }

    fun onCancel() {
        val domainWrapper = AutofillLogUtil.extractDomainFrom(
            urlDomain = website?.toUrlDomainOrNull(),
            packageName = packageName
        )
        logger.onCancel(domainWrapper)
        stateFlow.tryEmit(AutofillCreateAccountState.Cancelled(stateFlow.value.data))
    }
}