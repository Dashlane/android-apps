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
import com.dashlane.common.logger.developerinfo.DeveloperInfoLogger
import com.dashlane.credentialmanager.model.PrivilegedAllowlist
import com.dashlane.credentialmanager.model.formatHexString
import com.dashlane.credentialsmanager.R
import com.dashlane.ext.application.TrustedBrowserApplication
import com.dashlane.util.stackTraceToSafeString
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
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
    lateinit var developerInfoLogger: DeveloperInfoLogger

    @Inject
    lateinit var moshi: Moshi

    override fun onBeginGetCredentialRequest(
        request: BeginGetCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginGetCredentialResponse, GetCredentialException>
    ) {
        if (!verifyOrigin(request.callingAppInfo)) {
            callback.onError(GetCredentialUnsupportedException("Unsupported origin"))
            return
        }
        try {
            
            if (!credentialManagerLocker.isLoggedIn()) {
                callback.onResult(
                    BeginGetCredentialResponse.Builder()
                        .addAuthenticationAction(
                            AuthenticationAction(
                                getString(R.string.credential_manager_locked),
                                credentialManagerIntent.loginToDashlaneIntent()
                            )
                        )
                        .build()
                )
            } else {
                val credentialEntries = credentialManagerHandler.handleGetCredentials(this, request)
                callback.onResult(
                    BeginGetCredentialResponse.Builder()
                        .setCredentialEntries(credentialEntries)
                        .build()
                )
            }
        } catch (e: Exception) {
            developerInfoLogger.log(
                "credential_manager_get",
                getErrorMessage(request.callingAppInfo),
                e.stackTraceToSafeString()
            )
            callback.onError(GetCredentialUnknownException())
        }
    }

    override fun onBeginCreateCredentialRequest(
        request: BeginCreateCredentialRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<BeginCreateCredentialResponse, CreateCredentialException>
    ) {
        if (!verifyOrigin(request.callingAppInfo)) {
            callback.onError(CreateCredentialUnsupportedException("Unsupported origin"))
            return
        }
        try {
            val createEntries = credentialManagerHandler.handleCreateCredential(this, request)
            callback.onResult(
                BeginCreateCredentialResponse.Builder()
                    .setCreateEntries(createEntries)
                    .build()
            )
        } catch (e: Exception) {
            developerInfoLogger.log(
                "credential_manager_create",
                getErrorMessage(request.callingAppInfo),
                e.stackTraceToSafeString()
            )
            callback.onError(CreateCredentialUnknownException())
        }
    }

    override fun onClearCredentialStateRequest(
        request: ProviderClearCredentialStateRequest,
        cancellationSignal: CancellationSignal,
        callback: OutcomeReceiver<Void?, ClearCredentialException>
    ) {
        callback.onError(ClearCredentialUnknownException())
    }

    private fun verifyOrigin(callingAppInfo: CallingAppInfo?): Boolean {
        val privilegedAllowlist = PrivilegedAllowlist(
            apps = TrustedBrowserApplication.getAllTrustedBrowsers().map {
                PrivilegedAllowlist.PrivilegedAllowlistType(
                    type = "android",
                    info = PrivilegedAllowlist.PrivilegedAllowlistApp(
                        packageName = it.packageName,
                        signatures = it.signatures?.sha256Signatures?.map { sha256 ->
                            PrivilegedAllowlist.PrivilegedAllowlistSignature(
                                build = "release",
                                sha256 = formatHexString(sha256)
                            )
                        } ?: emptyList()
                    )
                )
            }
        )
        return try {
            callingAppInfo?.getOrigin(moshi.adapter(PrivilegedAllowlist::class.java).toJson(privilegedAllowlist))
            true
        } catch (e: IllegalStateException) {
            developerInfoLogger.log(
                "credential_manager_origin_verification",
                "Impossible to verify browser => ${callingAppInfo?.packageName}"
            )
            false
        }
    }

    private fun getErrorMessage(callingAppInfo: CallingAppInfo?) =
        "Request error from ${callingAppInfo?.packageName} with origin ${callingAppInfo?.origin}"
}