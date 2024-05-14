package com.dashlane.credentialmanager

import androidx.credentials.provider.CallingAppInfo
import com.dashlane.credentialmanager.model.PasskeyCreationOptions
import com.dashlane.credentialmanager.model.PasskeyRequestOptions
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.AlgorithmsSupported
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.AuthenticatorUserVerification
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.CeremonyStatus
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.PasskeyAuthenticationErrorType
import com.dashlane.hermes.generated.definitions.PasskeyRegistrationErrorType
import com.dashlane.hermes.generated.definitions.WebcardSaveOptions
import com.dashlane.hermes.generated.events.anonymous.AuthenticateWithPasskeyAnonymous
import com.dashlane.hermes.generated.events.anonymous.RegisterPasskeyAnonymous
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.UpdateVaultItem
import com.dashlane.teamspaces.getTeamSpaceLog
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class CredentialManagerLogger @Inject constructor(
    private val logRepository: LogRepository
) {
    fun logPasskeyCreate(
        request: PasskeyCreationOptions,
        callingAppInfo: CallingAppInfo,
        vaultItem: VaultItem<SyncObject.Passkey>?,
        status: CeremonyStatus,
        errorType: PasskeyRegistrationErrorType? = null
    ) {
        val domainType = if (callingAppInfo.origin == null) {
            DomainType.APP
        } else {
            DomainType.WEB
        }
        logRepository.queueEvent(
            RegisterPasskeyAnonymous(
                domain = Domain(Sha256Hash.of(request.rp.id), domainType),
                passkeyRegistrationStatus = status,
                algorithmsSupportedList = request.pubKeyCredParams
                    .mapNotNull { alg -> AlgorithmsSupported.values().firstOrNull { it.code == alg.type } },
                passkeyRegistrationErrorType = errorType
            )
        )
        logRepository.queueEvent(
            AutofillAccept(
                dataTypeList = listOf(ItemType.PASSKEY),
                webcardOptionSelected = WebcardSaveOptions.SAVE
            )
        )
        if (vaultItem != null) {
            logRepository.queueEvent(
                UpdateVaultItem(
                    itemId = ItemId(vaultItem.uid),
                    itemType = ItemType.PASSKEY,
                    action = Action.ADD,
                    space = vaultItem.getTeamSpaceLog(),
                )
            )
        }
    }

    fun logPasskeyLogin(
        request: PasskeyRequestOptions,
        callingAppInfo: CallingAppInfo,
        status: CeremonyStatus,
        errorType: PasskeyAuthenticationErrorType? = null
    ) {
        val domainType = if (callingAppInfo.origin == null) {
            DomainType.APP
        } else {
            DomainType.WEB
        }
        logRepository.queueEvent(
            AuthenticateWithPasskeyAnonymous(
                domain = Domain(Sha256Hash.of(request.rpId), domainType),
                passkeyAuthenticationStatus = status,
                isAuthenticatedWithDashlane = true,
                hasCredentialsAllowed = true,
                authenticatorUserVerification = AuthenticatorUserVerification.values()
                    .firstOrNull { it.name == request.userVerification },
                passkeyAuthenticationErrorType = errorType
            )
        )
        logRepository.queueEvent(
            AutofillAccept(
                dataTypeList = listOf(ItemType.PASSKEY)
            )
        )
    }

    fun logSuggestPasskeyCreate(callingAppInfo: CallingAppInfo?) {
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = AnyPage.AUTOFILL_NOTIFICATION_SAVE_PASSKEY
        )
        logRepository.queueEvent(
            AutofillSuggest(
                isNativeApp = callingAppInfo?.origin == null,
                vaultTypeList = listOf(ItemType.PASSKEY),
                webcardSaveOptions = listOf(WebcardSaveOptions.SAVE)
            )
        )
    }

    fun logSuggestPasskeyLogin(callingAppInfo: CallingAppInfo?, itemCount: Int) {
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = AnyPage.AUTOFILL_NOTIFICATION_AUTHENTICATE_PASSKEY
        )
        logRepository.queueEvent(
            AutofillSuggest(
                isNativeApp = callingAppInfo?.origin == null,
                vaultTypeList = listOf(ItemType.PASSKEY),
                webcardItemTotalCount = itemCount
            )
        )
    }
}