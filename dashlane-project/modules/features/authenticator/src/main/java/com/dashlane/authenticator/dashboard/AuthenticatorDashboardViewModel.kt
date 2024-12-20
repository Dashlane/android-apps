package com.dashlane.authenticator.dashboard

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.UserCommand
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.UserCommand.OtpSetupCommand
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.UserCommand.SeeAllCommand
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract.UserCommand.SeeLessCommand
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.UriParser
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardEditState.EditLogins
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardEditState.ViewLogins
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.HandleUri
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.HasLogins
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.HasLogins.CredentialItem
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.NoOtp
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.Progress
import com.dashlane.authenticator.otp
import com.dashlane.authenticator.updateOtp
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.lock.LockHelper
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.isSpaceItem
import com.dashlane.url.registry.UrlDomainRegistryFactory
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticatorDashboardViewModel @Inject constructor(
    urlDomainRegistryFactory: UrlDomainRegistryFactory,
    lockHelper: LockHelper,
    private val credentialDataQuery: CredentialDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private val dataSaver: DataSaver,
    private val dataSync: DataSync,
    private val logger: AuthenticatorLogger,
    private val frozenStateManager: FrozenStateManager,
    private val navigator: Navigator,
) : ViewModel(), AuthenticatorDashboardViewModelContract {
    private val userCommand = Channel<UserCommand>(1)
    private val editMode: Boolean
        get() = editState.value == EditLogins

    private val isAccountFrozen: Boolean
        get() = frozenStateManager.isAccountFrozen

    var otpUri: Uri? = null
    private lateinit var loadedCredentials: List<CredentialItem>

    override val urlDomainRegistry = urlDomainRegistryFactory.create()

    @OptIn(FlowPreview::class)
    override val uiState = userCommand.receiveAsFlow()
        .flatMapConcat { command ->
            when (command) {
                is OtpSetupCommand -> flow {
                    updateOtp(
                        command.itemId,
                        command.otp,
                        vaultDataQuery,
                        dataSaver,
                        dataSync,
                        logger,
                        updateModificationDate = command.updateModificationDate
                    )

                    emit(refreshOtpCredentials())
                }
                is SeeAllCommand -> flow {
                    emit(HasLogins(loadedCredentials, loadedCredentials.size, isAccountFrozen))
                }
                is SeeLessCommand -> flow {
                    emit(HasLogins(logins = loadedCredentials, isAccountFrozen = isAccountFrozen))
                }
            }
        }
        .onStart {
            lockHelper.waitUnlock()
            emit(refreshOtpCredentials())
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, WhileSubscribed(800, 800), Progress)

    override val editState = MutableStateFlow<AuthenticatorDashboardEditState>(ViewLogins)

    override fun onOtpCounterUpdate(itemId: String, otp: Otp) {
        userCommand.trySend(OtpSetupCommand(itemId, otp, updateModificationDate = false))
    }

    override fun onOtpCodeCopy(itemId: String, domain: String?): Boolean = if (isAccountFrozen) {
        navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
        false
    } else {
        logger.logCopyOtpCode(itemId, domain)
        true
    }

    override fun onSuccessAddOtp(itemId: String?, otp: Otp) = logger.logCompleteAdd2fa(itemId, otp)

    override fun onOtpSetup(itemId: String, otp: Otp) {
        userCommand.trySend(OtpSetupCommand(itemId, otp))
    }

    override fun onOtpRemoved(itemId: String) {
        userCommand.trySend(OtpSetupCommand(itemId, null))
    }

    override fun onSeeAll() {
        userCommand.trySend(SeeAllCommand)
        logger.logClickSeeAll()
    }

    override fun onSeeLess() {
        userCommand.trySend(SeeLessCommand)
    }

    override fun onSetupAuthenticator(activityResultLauncher: ActivityResultLauncher<Unit?>) {
        if (isAccountFrozen) {
            navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        viewModelScope.launch(Dispatchers.Main) {
            activityResultLauncher.launch(null)
        }
    }

    override fun onSetupAuthenticatorFromUri(
        otpUri: Uri,
        setUpAuthenticatorResultContract: SetUpAuthenticatorResultContract
    ) {
        
        
        viewModelScope.launch(Dispatchers.Main) {
            if (uiState.value is HandleUri) {
                setUpAuthenticatorResultContract.handleOtpResult(
                    this@AuthenticatorDashboardViewModel,
                    UriParser.parse(otpUri)
                )
            }
        }
    }

    override fun onEditClicked() {
        if (isAccountFrozen) {
            navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            return
        }

        editState.value = EditLogins
    }

    override fun onBackToViewMode() {
        editState.value = ViewLogins
    }

    override fun getCredentials() = credentialDataQuery.queryAll()

    private suspend fun refreshOtpCredentials(): AuthenticatorDashboardUiState {
        val otpCredentials = getOtpCredentials(getCredentials())
        val originalCredentials = (uiState.value as? HasLogins)?.logins
        
        if (originalCredentials != null && originalCredentials.size == otpCredentials.size) {
            otpCredentials.forEach { otpCredential ->
                originalCredentials.firstOrNull { otpCredential.id == it.id }?.expanded?.let {
                    otpCredential.expanded = it
                }
            }
        } else {
            
            otpCredentials.firstOrNull()?.expanded = true
        }
        return when {
            otpUri != null -> HandleUri(otpUri!!).also {
                
                otpUri = null
            }
            otpCredentials.isEmpty() -> NoOtp
            else -> {
                loadedCredentials = otpCredentials
                HasLogins(logins = loadedCredentials, isAccountFrozen = isAccountFrozen)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun getOtpCredentials(credentials: List<SummaryObject.Authentifiant>) =
        vaultDataQuery.queryAll(
            vaultFilter {
                specificUid(credentials.filter { it.hasOtpUrl }.map { it.id })
                specificDataType(SyncObjectType.AUTHENTIFIANT)
            }
        ).sortedByDescending { it.syncObject.userModificationDatetime }.mapNotNull {
            it as VaultItem<SyncObject.Authentifiant>
            
            val otp = it.syncObject.otp() ?: return@mapNotNull null
            CredentialItem(
                it.uid,
                it.titleForListNormalized ?: "",
                it.urlDomain,
                it.loginForUi,
                it.isSpaceItem() && it.syncObject.spaceId.isNotSemanticallyNull(),
                otp,
                expanded = editMode,
                editMode = editMode
            )
        }
}