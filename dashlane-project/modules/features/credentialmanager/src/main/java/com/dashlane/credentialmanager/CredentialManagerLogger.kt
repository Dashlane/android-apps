package com.dashlane.credentialmanager

import androidx.credentials.provider.CallingAppInfo
import com.dashlane.credentialmanager.model.fido.request.PasskeyCreationOptions
import com.dashlane.credentialmanager.model.fido.request.PasskeyRequestOptions
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.activityLog
import com.dashlane.hermes.generated.definitions.AlgorithmsSupported
import com.dashlane.hermes.generated.definitions.AuthenticatorUserVerification
import com.dashlane.hermes.generated.definitions.CeremonyStatus
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.PasskeyAuthenticationErrorType
import com.dashlane.hermes.generated.definitions.PasskeyRegistrationErrorType
import com.dashlane.hermes.generated.definitions.PasskeyType
import com.dashlane.hermes.generated.events.anonymous.AuthenticateWithPasskeyAnonymous
import com.dashlane.hermes.generated.events.anonymous.RegisterPasskeyAnonymous
import com.dashlane.hermes.generated.events.user.AuthenticateWithPasskey
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.RegisterPasskey
import com.dashlane.passkeys.algorithm.PasskeyAlgorithm
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.Properties
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.shouldCaptureActivityLog
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class CredentialManagerLogger @Inject constructor(
    private val logRepository: LogRepository,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
) {
    fun logPasskeyCreate(
        requestOptions: PasskeyCreationOptions,
        callingAppInfo: CallingAppInfo,
        vaultItem: VaultItem<SyncObject.Passkey>?,
        status: CeremonyStatus,
        errorType: PasskeyRegistrationErrorType? = null,
    ) {
        val domainType = callingAppInfo.toDomainType()
        val passkeyType = vaultItem?.syncObject?.toPasskeyType()
        logRepository.queueEvent(
            RegisterPasskeyAnonymous(
                domain = Domain(Sha256Hash.of(requestOptions.rp.id), domainType),
                passkeyRegistrationStatus = status,
                algorithmsSupportedList = requestOptions.pubKeyCredParams
                    .mapNotNull { alg -> AlgorithmsSupported.entries.firstOrNull { it.code == alg.type } },
                passkeyRegistrationErrorType = errorType,
                passkeyType = passkeyType,
            ),
        )
        logRepository.queueEvent(
            RegisterPasskey(
                passkeyRegistrationErrorType = errorType,
                passkeyType = passkeyType,
                passkeyRegistrationStatus = status,
            ),
        )
    }

    fun logPasskeyLogin(
        requestOptions: PasskeyRequestOptions,
        callingAppInfo: CallingAppInfo,
        status: CeremonyStatus,
        vaultItem: VaultItem<SyncObject.Passkey>?,
        errorType: PasskeyAuthenticationErrorType? = null,
    ) {
        val domainType = callingAppInfo.toDomainType()
        val passkeyType = vaultItem?.syncObject?.toPasskeyType()
        logRepository.queueEvent(
            AuthenticateWithPasskeyAnonymous(
                domain = Domain(Sha256Hash.of(requestOptions.rpId), domainType),
                passkeyAuthenticationStatus = status,
                isAuthenticatedWithDashlane = true,
                hasCredentialsAllowed = true,
                authenticatorUserVerification = AuthenticatorUserVerification.entries
                    .firstOrNull { it.code == requestOptions.userVerification },
                passkeyAuthenticationErrorType = errorType,
                passkeyType = passkeyType,
            ),
        )
        logRepository.queueEvent(
            AuthenticateWithPasskey(
                passkeyType = passkeyType,
                passkeyAuthenticationErrorType = errorType,
                passkeyAuthenticationStatus = status,
            ),
        )
        logRepository.queueEvent(
            AutofillAccept(
                dataTypeList = listOf(ItemType.PASSKEY),
            ),
        )
    }

    fun logSuggestPasskeyLogin(callingAppInfo: CallingAppInfo?, itemCount: Int) {
        logRepository.queueEvent(
            AutofillSuggest(
                isNativeApp = callingAppInfo?.isOriginPopulated() == false,
                vaultTypeList = listOf(ItemType.PASSKEY),
                webcardItemTotalCount = itemCount,
            ),
        )
    }

    fun activityLogPasskeyLogin(
        vaultItem: VaultItem<SyncObject.Passkey>,
        currentDomain: String,
    ) {
        sendActivityLog(
            summary = vaultItem,
            properties = Properties(
                passkeyDomain = vaultItem.syncObject.urlForGoToWebsite,
                currentDomain = currentDomain,
                credentialLogin = vaultItem.syncObject.userDisplayName ?: vaultItem.syncObject.userHandle,
            ),
        )
    }

    private fun sendActivityLog(
        summary: VaultItem<*>,
        properties: Properties,
    ) {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return
        if (!summary.shouldCaptureActivityLog(teamSpaceAccessor)) return
        logRepository.queueEvent(
            activityLog(
                logType = ActivityLog.LogType.USER_AUTHENTICATED_WITH_PASSKEY,
                properties = properties,
            ),
        )
    }

    private fun CallingAppInfo.toDomainType(): DomainType = if (isOriginPopulated()) {
        DomainType.WEB
    } else {
        DomainType.APP
    }

    private fun SyncObject.Passkey.toPasskeyType(): PasskeyType? =
        when (keyAlgorithm?.let { PasskeyAlgorithm.provider(it) }) {
            PasskeyAlgorithm.Cloud -> PasskeyType.CLOUD
            is PasskeyAlgorithm.Local -> PasskeyType.LEGACY
            null -> PasskeyType.LEGACY
        }
}