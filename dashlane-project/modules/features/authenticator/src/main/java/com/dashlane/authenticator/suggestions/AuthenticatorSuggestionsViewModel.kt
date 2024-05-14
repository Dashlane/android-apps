package com.dashlane.authenticator.suggestions

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.UserCommand
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.UserCommand.OtpSetupCommand
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.UserCommand.SeeAllCommand
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.UserCommand.SeeLessCommand
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.AllSetup
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.NoLogins
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.Progress
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.SetupComplete
import com.dashlane.authenticator.updateOtp
import com.dashlane.ext.application.KnownLinkedDomains.getMatchingLinkedDomainSet
import com.dashlane.lock.LockHelper
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.url.domain.otp.HardcodedOtpDomainsRepository
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.getAllLinkedPackageName
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AuthenticatorSuggestionsViewModel @Inject constructor(
    urlDomainRegistryFactory: UrlDomainRegistryFactory,
    lockHelper: LockHelper,
    private val vaultDataQuery: VaultDataQuery,
    private val credentialDataQuery: CredentialDataQuery,
    private val dataSaver: DataSaver,
    private val logger: AuthenticatorLogger,
    private val userPreferencesManager: UserPreferencesManager,
) : ViewModel(), AuthenticatorSuggestionsViewModelContract {
    private val repository = HardcodedOtpDomainsRepository()
    private val userCommand = Channel<UserCommand>(1)
    private lateinit var loadedCredentials: List<CredentialItem>
    private var otpCredentialsCount = -1

    override val urlDomainRegistry = urlDomainRegistryFactory.create()
    override val isFirstVisit: Boolean
        get() = !userPreferencesManager.isAuthenticatorGetStartedDisplayed

    @OptIn(FlowPreview::class)
    override val uiState = userCommand.receiveAsFlow()
        .flatMapConcat { command ->
            when (command) {
                is SeeAllCommand -> flow {
                    emit(HasLogins(loadedCredentials, loadedCredentials.size))
                }
                is SeeLessCommand -> flow {
                    emit(HasLogins(loadedCredentials))
                }
                is OtpSetupCommand -> flow {
                    emit(Progress)
                    saveOtp(command)
                    emit(SetupComplete)
                }
            }
        }
        .onStart {
            lockHelper.waitUnlock()
            emit(refreshCompatibleLogins())
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, WhileSubscribed(5_000), Progress)

    override fun onOtpSetup(itemId: String, otp: Otp) {
        userCommand.trySend(OtpSetupCommand(itemId, otp))
    }

    override fun onSuccessAddOtp(itemId: String?, otp: Otp) = logger.logCompleteAdd2fa(itemId, otp)

    override fun onSeeAll() {
        userCommand.trySend(SeeAllCommand)
        logger.logClickSeeAll()
    }

    override fun onSeeLess() {
        userCommand.trySend(SeeLessCommand)
    }

    override fun onSetupAuthenticator(activityResultLauncher: ActivityResultLauncher<Unit?>) {
        viewModelScope.launch(Dispatchers.Main) {
            activityResultLauncher.launch(null)
        }
    }

    override fun onOnboardingDisplayed() {
        userPreferencesManager.isAuthenticatorGetStartedDisplayed = true
    }

    override fun getCredentials() = credentialDataQuery.queryAll()

    private fun refreshCompatibleLogins(): AuthenticatorSuggestionsUiState {
        val credentials = getCredentials()
        val otpSetupCount = getCredentialsWithOtpCount(credentials)
        if (otpCredentialsCount != -1 && otpSetupCount != otpCredentialsCount) {
            
            return SetupComplete
        }
        otpCredentialsCount = otpSetupCount
        val compatibleCredentials = getCompatibleCredentials(credentials)
        val credentialToSecure = getReadyToSecureCredentialItems(compatibleCredentials)
        return when {
            compatibleCredentials.isEmpty() -> NoLogins
            credentialToSecure.isEmpty() -> AllSetup
            else -> {
                loadedCredentials = credentialToSecure
                HasLogins(loadedCredentials)
            }
        }
    }

    private suspend fun saveOtp(command: OtpSetupCommand) = updateOtp(
        command.itemId,
        command.otp,
        vaultDataQuery,
        dataSaver,
        logger,
        updateModificationDate = command.updateModificationDate
    )

    private fun getReadyToSecureCredentialItems(credentials: List<SummaryObject.Authentifiant>) =
        credentials.filter { !it.hasOtpUrl }
            .sortedByDescending { it.locallyUsedCount }
            .map {
                CredentialItem(
                    it.id,
                    it.titleForListNormalized,
                    it.urlDomain,
                    it.loginForUi,
                    it.linkedServices.getAllLinkedPackageName().firstOrNull(),
                    it.isProfessional()
                )
            }

    private fun getCompatibleCredentials(credentials: List<SummaryObject.Authentifiant>) =
        credentials.filter {
            val domain = it.urlDomain?.toUrlDomainOrNull() ?: return@filter false
            repository.isSupportingOtp(domain) || getMatchingLinkedDomainSet(domain.value)?.any { linkedDomain ->
                repository.isSupportingOtp(linkedDomain)
            } == true
        }

    private fun getCredentialsWithOtpCount(credentials: List<SummaryObject.Authentifiant>) =
        credentials.count { it.hasOtpUrl }

    private fun SummaryObject.Authentifiant.isProfessional() =
        isSpaceItem() && spaceId.isNotSemanticallyNull()
}