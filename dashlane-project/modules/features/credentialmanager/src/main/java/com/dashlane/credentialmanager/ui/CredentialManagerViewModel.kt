package com.dashlane.credentialmanager.ui

import android.content.Intent
import androidx.annotation.RequiresApi
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.PendingIntentHandler
import androidx.credentials.provider.ProviderCreateCredentialRequest
import androidx.credentials.provider.ProviderGetCredentialRequest
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.credentialmanager.CredentialLoader
import com.dashlane.credentialmanager.CredentialManagerHandlerImpl
import com.dashlane.credentialmanager.CredentialManagerLocker
import com.dashlane.credentialmanager.credential.CredentialPasskeyManager
import com.dashlane.credentialmanager.credential.CredentialPasswordManager
import com.dashlane.credentialmanager.model.DashlaneCredentialManagerException
import com.dashlane.credentialmanager.model.ItemNotFoundException
import com.dashlane.credentialmanager.model.MissingArgumentException
import com.dashlane.credentialmanager.model.PasskeyRequestOptions
import com.dashlane.credentialmanager.model.PasswordLimitReachedException
import com.dashlane.credentialmanager.model.UnsupportedCredentialTypeException
import com.dashlane.limitations.PasswordLimiter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.xml.domain.SyncObject
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@RequiresApi(34)
@HiltViewModel
class CredentialManagerViewModel @Inject constructor(
    private val credentialManagerLocker: CredentialManagerLocker,
    private val credentialLoader: CredentialLoader,
    private val credentialPasskeyManager: CredentialPasskeyManager,
    private val credentialPasswordManager: CredentialPasswordManager,
    private val savedStateHandle: SavedStateHandle,
    private val passwordLimiter: PasswordLimiter
) : ViewModel() {
    private val stateFlow = MutableStateFlow<CredentialManagerState>(CredentialManagerState.Unlocking)
    val uiState = stateFlow.asStateFlow()

    private var isHandlingConfigurationChange: Boolean = false
    private var isUnlockActivityOpen = false

    private var getRequest: ProviderGetCredentialRequest? = null
    private var createRequest: ProviderCreateCredentialRequest? = null

    fun start(intent: Intent) {
        getRequest = PendingIntentHandler.retrieveProviderGetCredentialRequest(intent)
        createRequest = PendingIntentHandler.retrieveProviderCreateCredentialRequest(intent)
    }

    fun unlockDashlaneIfNeeded() {
        val isLocked = credentialManagerLocker.isAccountLocked()
        when {
            !isLocked -> onUnlockSuccess()
            !isUnlockActivityOpen -> {
                
                credentialManagerLocker.unlockDashlane()
                isUnlockActivityOpen = true
            }
            isHandlingConfigurationChange -> {
                
                isHandlingConfigurationChange = false
                return
            }
            else -> {
                
                stateFlow.tryEmit(CredentialManagerState.Error(ItemNotFoundException("User cancelled the unlock flow")))
            }
        }
    }

    fun onUnlockSuccess() {
        if (getRequest != null) {
            provideCredential(getRequest)
        } else if (createRequest != null) {
            createCredential(createRequest)
        } else {
            stateFlow.tryEmit(CredentialManagerState.Error(MissingArgumentException("No request found")))
        }
    }

    fun createCredential(createRequest: ProviderCreateCredentialRequest?) {
        viewModelScope.launch {
            try {
                requireNotNull(createRequest) { "CreateRequest is null" }
                val callingAppInfo = createRequest.callingAppInfo
                when (val request = createRequest.callingRequest) {
                    is CreatePublicKeyCredentialRequest -> {
                        credentialPasskeyManager.createPasskey(request, callingAppInfo)
                    }
                    is CreatePasswordRequest -> {
                        if (passwordLimiter.isPasswordLimitReached()) {
                            throw PasswordLimitReachedException("Password limit reached")
                        } else {
                            credentialPasswordManager.createPasswordLogin(request, callingAppInfo)
                        }
                    }
                    else -> throw IllegalArgumentException("Unsupported credential type")
                }.let {
                    stateFlow.emit(CredentialManagerState.CreateSuccess(it))
                }
            } catch (e: DashlaneCredentialManagerException) {
                stateFlow.emit(CredentialManagerState.Error(e))
            }
        }
    }

    fun provideCredential(getRequest: ProviderGetCredentialRequest?) {
        viewModelScope.launch {
            try {
                requireNotNull(getRequest) { "GetRequest is null" }
                val credentialItemId: String = savedStateHandle[CredentialManagerHandlerImpl.ARG_CREDENTIAL_ID]
                    ?: throw MissingArgumentException("Credential id should be provided")
                val vaultItem = credentialLoader.loadSyncObject(credentialItemId)
                when (val request = getRequest.credentialOptions[0]) {
                    is GetPublicKeyCredentialOption -> buildGetPasskeyCredential(
                        request,
                        vaultItem,
                        getRequest.callingAppInfo
                    )
                    is GetPasswordOption -> buildGetPasswordCredential(vaultItem)
                    else -> throw UnsupportedCredentialTypeException("${request.type} is not supported")
                }.let {
                    GetCredentialResponse(it)
                }.let {
                    stateFlow.emit(CredentialManagerState.AuthSuccess(it))
                }
            } catch (e: DashlaneCredentialManagerException) {
                stateFlow.emit(CredentialManagerState.Error(e))
            }
        }
    }

    private suspend fun buildGetPasskeyCredential(
        request: GetPublicKeyCredentialOption,
        vaultItem: VaultItem<SyncObject>?,
        callingAppInfo: CallingAppInfo
    ): PublicKeyCredential {
        val passkeyRequestOption = Gson().fromJson(request.requestJson, PasskeyRequestOptions::class.java)
        val passkeySyncObject = vaultItem?.asVaultItemOfClassOrNull(SyncObject.Passkey::class.java)
            ?: throw ItemNotFoundException("Passkey login not found")
        return credentialPasskeyManager.providePasskey(
            passkeyRequestOption,
            passkeySyncObject,
            callingAppInfo,
            request.clientDataHash
        )
    }

    private fun buildGetPasswordCredential(vaultItem: VaultItem<SyncObject>?): PasswordCredential {
        val passwordSyncObject = vaultItem?.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java)
            ?: throw ItemNotFoundException("Password login not found")
        return credentialPasswordManager.providePasswordLogin(passwordSyncObject)
    }

    fun setConfigurationChanging() {
        isHandlingConfigurationChange = true
    }
}