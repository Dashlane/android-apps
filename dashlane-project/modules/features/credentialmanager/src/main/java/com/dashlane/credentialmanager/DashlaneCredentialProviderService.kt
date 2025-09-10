package com.dashlane.credentialmanager

import android.os.Build
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.annotation.RequiresApi
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.ClearCredentialUnknownException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.CreateCredentialUnsupportedException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.provider.AuthenticationAction
import androidx.credentials.provider.BeginCreateCredentialRequest
import androidx.credentials.provider.BeginCreateCredentialResponse
import androidx.credentials.provider.BeginGetCredentialRequest
import androidx.credentials.provider.BeginGetCredentialResponse
import androidx.credentials.provider.CallingAppInfo
import androidx.credentials.provider.CredentialProviderService
import androidx.credentials.provider.ProviderClearCredentialStateRequest
import com.dashlane.credentialmanager.model.PasskeyPrivilegedApplications
import com.dashlane.credentialmanager.model.PrivilegedAllowlist
import com.dashlane.credentialsmanager.R
import com.dashlane.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@AndroidEntryPoint
class DashlaneCredentialProviderService : CredentialProviderService() {
    @Inject
    lateinit var credentialManagerIntent: CredentialManagerIntent

    @Inject
    lateinit var credentialManagerLocker: CredentialManagerLocker

    @Inject
    lateinit var credentialManagerHandler: CredentialManagerHandler

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onBeginGetCredentialRequest(
        request: BeginGetCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>,
    ) {
        if (!verifyOrigin(request.callingAppInfo)) {
            callback.onError(GetCredentialUnsupportedException("Unsupported origin"))
            return
        }
        try {
            
            if (!credentialManagerLocker.isLoggedIn()) {
                callback.onResult(
                    BeginGetCredentialResponse(
                        authenticationActions = listOf(
                            AuthenticationAction(
                                title = getString(R.string.credential_manager_locked),
                                pendingIntent = credentialManagerIntent.loginToDashlaneIntent(),
                            ),
                        ),
                    ),
                )
            } else {
                val credentialEntries = credentialManagerHandler.handleGetCredentials(
                    context = this,
                    request = request,
                    username = sessionManager.session?.username,
                )
                callback.onResult(BeginGetCredentialResponse(credentialEntries))
            }
        } catch (e: Exception) {
            callback.onError(GetCredentialUnknownException())
        }
    }

    override fun onBeginCreateCredentialRequest(
        request: BeginCreateCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>,
    ) {
        if (!verifyOrigin(request.callingAppInfo)) {
            callback.onError(CreateCredentialUnsupportedException("Unsupported origin"))
            return
        }
        try {
            val createEntries = credentialManagerHandler.handleCreateCredential(
                context = this,
                request = request,
                username = sessionManager.session?.username,
            )
            callback.onResult(BeginCreateCredentialResponse(createEntries))
        } catch (e: Exception) {
            callback.onError(CreateCredentialUnknownException())
        }
    }

    override fun onClearCredentialStateRequest(
        request: ProviderClearCredentialStateRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<Void?, ClearCredentialException>,
    ) {
        callback.onError(ClearCredentialUnknownException())
    }

    private fun verifyOrigin(callingAppInfo: CallingAppInfo?): Boolean {
        val privilegedAllowlist = PasskeyPrivilegedApplications.allowList
        return try {
            val json = Json.encodeToString(PrivilegedAllowlist.serializer(), privilegedAllowlist)
            callingAppInfo?.getOrigin(json)
            true
        } catch (_: IllegalStateException) {
            false
        }
    }
}