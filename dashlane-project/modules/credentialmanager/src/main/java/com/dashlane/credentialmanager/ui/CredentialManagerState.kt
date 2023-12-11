package com.dashlane.credentialmanager.ui

import androidx.credentials.CreateCredentialResponse
import androidx.credentials.GetCredentialResponse
import com.dashlane.credentialmanager.model.DashlaneCredentialManagerException

sealed class CredentialManagerState {
    object Unlocking : CredentialManagerState()
    data class CreateSuccess(val response: CreateCredentialResponse) : CredentialManagerState()
    data class AuthSuccess(val response: GetCredentialResponse) : CredentialManagerState()
    data class Error(val e: DashlaneCredentialManagerException) : CredentialManagerState()
}
