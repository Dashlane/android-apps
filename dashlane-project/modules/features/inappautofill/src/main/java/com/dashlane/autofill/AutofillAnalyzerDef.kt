package com.dashlane.autofill

import android.content.Context
import com.dashlane.autofill.phishing.PhishingAttemptLevel
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.url.UrlDomain
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.time.Instant
import java.time.Month
import java.time.Year
import com.dashlane.hermes.generated.definitions.AutofillOrigin as HermesAutofillOrigin

interface AutofillAnalyzerDef {
    interface DatabaseAccess {
        fun clearCache()
        val isLoggedIn: Boolean
        fun loadAuthentifiantsByPackageName(packageName: String): List<SummaryObject.Authentifiant>?
        fun loadAuthentifiantsByUrl(url: String): List<SummaryObject.Authentifiant>?
        fun <T : SummaryObject> loadSummaries(type: SyncObjectType): List<T>?

        fun <T : SummaryObject> loadSummary(uid: String): T?

        fun <T : SyncObject> loadSyncObject(itemId: String): VaultItem<T>?

        suspend fun createNewAuthentifiantFromAutofill(
            context: Context,
            title: String?,
            website: String?,
            login: String,
            password: String,
            packageName: String?,
            spaceId: String?
        ): VaultItem<SyncObject.Authentifiant>?

        suspend fun saveAuthentifiant(
            context: Context,
            website: String?,
            packageName: String,
            login: String?,
            password: String?
        ): VaultItem<SyncObject.Authentifiant>?

        suspend fun updateAuthentifiantWebsite(
            uid: String,
            website: String
        ): VaultItem<SyncObject.Authentifiant>?

        suspend fun updateLastViewDate(
            itemId: String,
            instant: Instant
        )

        suspend fun saveCreditCard(
            number: String?,
            securityCode: String?,
            expireMonth: Month?,
            expireYear: Year?
        ): VaultItem<SyncObject.PaymentCreditCard>?

        suspend fun updateAuthentifiantPassword(
            uid: String?,
            password: String?
        ): VaultItem<SyncObject.Authentifiant>?

        suspend fun addAuthentifiantLinkedWebDomain(uid: String, website: String): Boolean

        suspend fun addAuthentifiantLinkedApp(uid: String, packageName: String): Boolean

        val authentifiantCount: Int
    }

    interface IAutofillSecurityApplication {
        fun getSignatureVerification(
            context: Context,
            packageName: String,
            authentifiant: SummaryObject.Authentifiant
        ): SignatureVerification
    }

    interface IAutofillUsageLog {
        fun onLoginFormFound(packageName: String)
        fun onShowCredentialsList(
            @AutofillOrigin origin: Int,
            packageName: String,
            isNativeApp: Boolean,
            totalCount: Int,
            phishingAttemptLevel: PhishingAttemptLevel
        )

        fun onShowCreditCardList(
            @AutofillOrigin origin: Int,
            packageName: String,
            isNativeApp: Boolean,
            totalCount: Int,
            phishingAttemptLevel: PhishingAttemptLevel,
        )

        fun onShowEmailList(
            @AutofillOrigin origin: Int,
            packageName: String,
            isNativeApp: Boolean,
            totalCount: Int,
            phishingAttemptLevel: PhishingAttemptLevel
        )

        fun onAutoFillCredentialDone(
            @AutofillOrigin origin: Int,
            packageName: String,
            websiteUrlDomain: UrlDomain?,
            itemUrlDomain: UrlDomain?,
            matchType: MatchType,
            autofillOrigin: HermesAutofillOrigin,
            autofillMechanism: AutofillMechanism,
            credentialId: String?,
        )

        fun onAutoFillCreditCardDone(
            @AutofillOrigin origin: Int,
            packageName: String,
            websiteUrlDomain: UrlDomain?,
            matchType: MatchType,
            autofillOrigin: HermesAutofillOrigin,
            autofillMechanism: AutofillMechanism,
            credentialId: String,
        )

        fun onAutoFillEmailDone(
            @AutofillOrigin origin: Int,
            packageName: String,
            websiteUrlDomain: UrlDomain?,
            matchType: MatchType,
            autofillOrigin: HermesAutofillOrigin,
            autofillMechanism: AutofillMechanism,
            credentialId: String,
        )

        fun onAutoFillWarningClick()
    }

    interface ILockManager {
        fun showLockActivityForAutofillApi(context: Context)
        fun showLockActivityForInAppLogin(context: Context, itemUID: String?)
        val isInAppLoginLocked: Boolean
        fun logoutAndCallLoginScreenForInAppLogin(context: Context)
    }

    interface IUserPreferencesAccess {
        fun hasKeyboardAutofillEnabled(): Boolean

        fun hasAutomatic2faTokenCopy(): Boolean
    }
}