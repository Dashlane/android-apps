package com.dashlane.autofill

import android.content.Context
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.api.unlockfill.SupportedAutofills
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.url.UrlDomain
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import java.time.Instant
import java.time.Month
import java.time.Year
import com.dashlane.hermes.generated.definitions.AutofillOrigin as HermesAutofillOrigin

interface AutofillAnalyzerDef {
    interface DatabaseAccess {
        fun clearCache()
        val isLoggedIn: Boolean
        fun loadAuthentifiant(uid: String): VaultItem<SyncObject.Authentifiant>?
        fun loadSummaryAuthentifiant(uid: String): SummaryObject.Authentifiant?
        fun loadAllAuthentifiant(): List<SummaryObject.Authentifiant>
        fun loadCreditCard(uid: String): VaultItem<SyncObject.PaymentCreditCard>?
        fun loadEmail(uid: String): SummaryObject.Email?
        fun loadAddress(uid: String): SummaryObject.Address?
        fun loadAuthentifiantsByPackageName(packageName: String): List<SummaryObject.Authentifiant>?
        fun loadAuthentifiantsByUrl(url: String): List<SummaryObject.Authentifiant>?
        fun loadCreditCards(): List<SummaryObject.PaymentCreditCard>?
        fun loadEmails(): List<SummaryObject.Email>?
        fun loadAddresses(uids: Collection<String>): List<SummaryObject.Address>?

        

        suspend fun createNewAuthentifiantFromAutofill(
            context: Context,
            title: String?,
            website: String?,
            login: String,
            password: String,
            packageName: String?
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
        ): SummaryObject.Authentifiant?

        

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
        fun onManualRequestAsked(@AutofillOrigin origin: Int, packageName: String)
        fun onLoginFormFound(packageName: String)
        fun onShowCredentialsList(
            @AutofillOrigin origin: Int,
            packageName: String,
            isNativeApp: Boolean,
            totalCount: Int
        )

        fun onShowCreditCardList(
            @AutofillOrigin origin: Int,
            packageName: String,
            isNativeApp: Boolean,
            totalCount: Int
        )

        fun onShowEmailList(
            @AutofillOrigin origin: Int,
            packageName: String,
            isNativeApp: Boolean,
            totalCount: Int
        )

        fun onShowSmsOtp(@AutofillOrigin origin: Int, packageName: String)
        fun onShowLogout(@AutofillOrigin origin: Int, packageName: String)
        fun onNoResultsForCredential(@AutofillOrigin origin: Int, packageName: String)
        fun onNoResultsForCreditCard(@AutofillOrigin origin: Int, packageName: String)
        fun onNoResultsForEmail(@AutofillOrigin origin: Int, packageName: String)
        fun onClickToAutoFillSuggestion(
            @AutofillOrigin origin: Int,
            packageName: String?,
            website: UrlDomain?,
            supportedAutofills: SupportedAutofills?
        )

        fun onClickToAutoFillCredentialButLock(@AutofillOrigin origin: Int, itemUrl: String?)
        fun onClickToAutoFillCreditCardButLock(@AutofillOrigin origin: Int)
        fun onClickToAutoFillCredentialNotLock(
            @AutofillOrigin origin: Int,
            packageName: String,
            itemUrl: String?
        )

        fun onClickToAutoFillCreditCardNotLock(@AutofillOrigin origin: Int, packageName: String)
        fun onClickToAutoFillSmsOtp(@AutofillOrigin origin: Int, packageName: String)
        fun onAutoFillCredentialDone(
            @AutofillOrigin origin: Int,
            packageName: String,
            websiteUrlDomain: UrlDomain?,
            itemUrlDomain: UrlDomain?,
            autofillFeature: AutofillFeature,
            matchType: MatchType,
            autofillOrigin: HermesAutofillOrigin,
            autofillMechanism: AutofillMechanism
        )

        fun onAutoFillCreditCardDone(
            @AutofillOrigin origin: Int,
            packageName: String,
            websiteUrlDomain: UrlDomain?,
            autofillFeature: AutofillFeature,
            matchType: MatchType,
            autofillOrigin: HermesAutofillOrigin,
            autofillMechanism: AutofillMechanism
        )

        fun onAutoFillEmailDone(
            @AutofillOrigin origin: Int,
            packageName: String,
            websiteUrlDomain: UrlDomain?,
            autofillFeature: AutofillFeature,
            matchType: MatchType,
            autofillOrigin: HermesAutofillOrigin,
            autofillMechanism: AutofillMechanism
        )

        fun onAutoFillSmsOtpDone(@AutofillOrigin origin: Int, packageName: String)
        fun saveCredentialCancelLogout(@AutofillOrigin origin: Int, packageName: String)
        fun saveCreditCardCancelLogout(@AutofillOrigin origin: Int, packageName: String)
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