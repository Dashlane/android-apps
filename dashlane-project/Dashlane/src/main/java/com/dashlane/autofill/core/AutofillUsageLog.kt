package com.dashlane.autofill.core

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.AutofillOrigin.AUTO_FILL_API
import com.dashlane.autofill.AutofillOrigin.INLINE_AUTOFILL_KEYBOARD
import com.dashlane.autofill.AutofillOrigin.IN_APP_LOGIN
import com.dashlane.autofill.api.fillresponse.relatedOnlyByLinkedDomains
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.api.unlockfill.SupportedAutofills
import com.dashlane.autofill.api.util.AutofillLogUtil
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.AutofillOrigin as HermesAutofillOrigin
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.FieldsFilled
import com.dashlane.hermes.generated.definitions.FormType
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.hermes.generated.events.anonymous.AutofillSuggestAnonymous
import com.dashlane.hermes.generated.events.anonymous.PerformAutofillAnonymous
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.PerformAutofill
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.url.UrlDomain
import com.dashlane.useractivity.log.install.InstallLogCode42
import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode5
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogCode96
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject



@Suppress("LargeClass")
class AutofillUsageLog @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    installLogRepository: InstallLogRepository,
    private val logRepository: LogRepository
) : AutofillAnalyzerDef.IAutofillUsageLog, AutofillLegacyLogger(
    sessionManager, bySessionUsageLogRepository, installLogRepository
) {

    private var lastLoginFormFoundPackage: String? = null

    override fun onManualRequestAsked(@AutofillOrigin origin: Int, packageName: String) {
        log(
            UsageLogCode96(
                action = UsageLogCode96.Action.AUTOFILL_MANUAL_REQUEST,
                app = packageName,
                sender = getSenderUsage96(origin)
            )
        )
    }

    override fun onLoginFormFound(packageName: String) {
        if (lastLoginFormFoundPackage != null && lastLoginFormFoundPackage == packageName) {
            return
        }
        lastLoginFormFoundPackage = packageName
    }

    override fun onShowCredentialsList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int
    ) {
        onShowList(
            origin = origin,
            packageName = packageName,
            type = UsageLogCode96.Type.AUTHENTICATION,
            itemType = ItemType.CREDENTIAL,
            isNativeApp = isNativeApp,
            totalCount = totalCount
        )
    }

    override fun onShowCreditCardList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int
    ) {
        onShowList(
            origin = origin,
            packageName = packageName,
            type = UsageLogCode96.Type.PAYMENT_MEAN_CREDITCARD,
            itemType = ItemType.CREDIT_CARD,
            isNativeApp = isNativeApp,
            totalCount = totalCount
        )
    }

    override fun onShowEmailList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int
    ) {
        onShowList(
            origin = origin, packageName = packageName,
            type = UsageLogCode96.Type.EMAIL,
            itemType = ItemType.CREDIT_CARD,
            isNativeApp = isNativeApp,
            totalCount = totalCount
        )
    }

    override fun onShowSmsOtp(origin: Int, packageName: String) {
        sendInstallLog42(origin, packageName, InstallLogCode42.Action.SHOW_SMS_OTP_PROMPT)
    }

    override fun onShowLogout(@AutofillOrigin origin: Int, packageName: String) {
        sendInstallLog42(origin, packageName, InstallLogCode42.Action.SHOW_IMPALA)
    }

    override fun onNoResultsForCredential(origin: Int, packageName: String) {
        onNoResults(origin, packageName, UsageLogCode96.Type.AUTHENTICATION)
    }

    override fun onNoResultsForCreditCard(origin: Int, packageName: String) {
        onNoResults(origin, packageName, UsageLogCode96.Type.PAYMENT_MEAN_CREDITCARD)
    }

    override fun onNoResultsForEmail(origin: Int, packageName: String) {
        onNoResults(origin, packageName, UsageLogCode96.Type.EMAIL)
    }

    override fun onClickToAutoFillSuggestion(
        origin: Int,
        packageName: String?,
        website: UrlDomain?,
        supportedAutofills: SupportedAutofills?
    ) {
        logClickToAutoFillSuggestion(
            origin,
            packageName,
            website,
            supportedAutofills.toUsageLog96Type()
        )
    }

    fun logClickToAutoFillSuggestion(
        origin: Int,
        packageName: String?,
        website: UrlDomain?,
        type: UsageLogCode96.Type?
    ) {
        log(
            UsageLogCode96(
                app = packageName,
                sender = getSenderUsage96(origin),
                action = UsageLogCode96.Action.CLICK_SUGGESTION,
                type = type,
                hasCredentials = true,
                webappDomain = website?.value
            )
        )
    }

    override fun onClickToAutoFillCredentialButLock(@AutofillOrigin origin: Int, itemUrl: String?) {
        onClickToAutoFillButLock(UsageLogConstant.LockAction.unlock, itemUrl)
    }

    override fun onClickToAutoFillCreditCardButLock(@AutofillOrigin origin: Int) {
        onClickToAutoFillButLock(UsageLogConstant.LockAction.unlockCreditCard, null)
    }

    override fun onClickToAutoFillCredentialNotLock(
        @AutofillOrigin origin: Int,
        packageName: String,
        itemUrl: String?
    ) {
        onClickToAutoFillNotLock(origin, packageName, itemUrl, UsageLogCode96.Type.AUTHENTICATION)
    }

    override fun onClickToAutoFillCreditCardNotLock(
        @AutofillOrigin origin: Int,
        packageName: String
    ) {
        onClickToAutoFillNotLock(
            origin,
            packageName,
            null,
            UsageLogCode96.Type.PAYMENT_MEAN_CREDITCARD
        )
    }

    override fun onClickToAutoFillSmsOtp(@AutofillOrigin origin: Int, packageName: String) {
        sendInstallLog42(origin, packageName, InstallLogCode42.Action.CLICK_SMS_OTP_PROMPT)
    }

    override fun onAutoFillCredentialDone(
        @AutofillOrigin origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        itemUrlDomain: UrlDomain?,
        autofillFeature: AutofillFeature,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism
    ) {
        logPerformAutofill(
            websiteUrlDomain = websiteUrlDomain,
            packageName = packageName,
            autofillOrigin = autofillOrigin,
            autofillMechanism = autofillMechanism,
            matchType = matchType,
            fieldsFilled = FieldsFilled(credential = 1),
            formType = FormType.LOGIN
        )

        val credentialOrigin = when {
            autofillFeature == AutofillFeature.VIEW_ALL_ACCOUNTS -> UsageLogCode5.CredentialOrigin.VIEW_ALL_ACCOUNTS
            websiteUrlDomain.relatedOnlyByLinkedDomains(itemUrlDomain) -> UsageLogCode5.CredentialOrigin.ASSOCIATED_WEBSITE
            else -> UsageLogCode5.CredentialOrigin.CLASSIC
        }

        log(
            UsageLogCode5(
                type = getTypeUsage5(origin),
                packageName = packageName,
                website = websiteUrlDomain?.value,
                vaultItemWebsite = itemUrlDomain?.value,
                authentication = 1,
                credentialOrigin = credentialOrigin
            )
        )
    }

    private fun logPerformAutofill(
        websiteUrlDomain: UrlDomain?,
        packageName: String,
        autofillOrigin: com.dashlane.hermes.generated.definitions.AutofillOrigin,
        autofillMechanism: AutofillMechanism,
        matchType: MatchType,
        fieldsFilled: FieldsFilled,
        formType: FormType
    ) {
        val domainWrapper = AutofillLogUtil.extractDomainFrom(urlDomain = websiteUrlDomain, packageName = packageName)
        logRepository.queueEvent(
            PerformAutofill(
                isAutologin = false,
                autofillOrigin = autofillOrigin,
                autofillMechanism = autofillMechanism,
                isManual = false,
                matchType = matchType,
                fieldsFilled = fieldsFilled,
                formTypeList = listOf(formType)
            )
        )

        logRepository.queueEvent(
            PerformAutofillAnonymous(
                isAutologin = false,
                autofillOrigin = autofillOrigin,
                domain = domainWrapper.domain,
                autofillMechanism = autofillMechanism,
                isManual = false,
                matchType = matchType,
                fieldsFilled = fieldsFilled,
                isNativeApp = domainWrapper.isNativeApp,
                formTypeList = listOf(formType)
            )
        )
    }

    override fun onAutoFillCreditCardDone(
        @AutofillOrigin origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        autofillFeature: AutofillFeature,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism
    ) {
        logPerformAutofill(
            websiteUrlDomain = websiteUrlDomain,
            packageName = packageName,
            autofillOrigin = autofillOrigin,
            autofillMechanism = autofillMechanism,
            matchType = matchType,
            fieldsFilled = FieldsFilled(creditCard = 1),
            formType = FormType.PAYMENT
        )

        log(
            UsageLogCode5(
                type = getTypeUsage5(origin),
                packageName = packageName,
                website = websiteUrlDomain?.value,
                paymentMeanCreditcard = 1
            )
        )
    }

    override fun onAutoFillEmailDone(
        origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        autofillFeature: AutofillFeature,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism
    ) {
        logPerformAutofill(
            websiteUrlDomain = websiteUrlDomain,
            packageName = packageName,
            autofillOrigin = autofillOrigin,
            autofillMechanism = autofillMechanism,
            matchType = matchType,
            fieldsFilled = FieldsFilled(email = 1),
            formType = FormType.LOGIN
        )

        log(
            UsageLogCode5(
                type = getTypeUsage5(origin),
                packageName = packageName,
                website = websiteUrlDomain?.value,
                email = 1
            )
        )
    }

    override fun onAutoFillSmsOtpDone(origin: Int, packageName: String) {
        sendInstallLog42(origin, packageName, InstallLogCode42.Action.FILL_SMS_OTP)
    }

    override fun saveCredentialCancelLogout(origin: Int, packageName: String) {
        val action = InstallLogCode42.Action.SAVE_ABORT_CREDENTIAL
        sendInstallLog42(origin, packageName, action)
    }

    override fun saveCreditCardCancelLogout(origin: Int, packageName: String) {
        sendInstallLog42(origin, packageName, InstallLogCode42.Action.SAVE_ABORT_CC)
    }

    private fun getTypeUsage5(@AutofillOrigin origin: Int): UsageLogCode5.Type? {
        return when (origin) {
            AUTO_FILL_API -> UsageLogCode5.Type.ANDROID_AUTOFILL_API
            IN_APP_LOGIN -> UsageLogCode5.Type.ANDROID_IN_APP_LOGIN
            INLINE_AUTOFILL_KEYBOARD -> UsageLogCode5.Type.ANDROID_AUTOFILL_API_KEYBOARD
            else -> null
        }
    }

    private fun onShowList(
        @AutofillOrigin origin: Int,
        packageName: String,
        type: UsageLogCode96.Type,
        itemType: ItemType,
        isNativeApp: Boolean,
        totalCount: Int
    ) {
        val domainType = if (isNativeApp) {
            DomainType.APP
        } else {
            DomainType.WEB
        }
        val page = when (origin) {
            INLINE_AUTOFILL_KEYBOARD -> AnyPage.AUTOFILL_KEYBOARD_SUGGESTION
            IN_APP_LOGIN -> AnyPage.AUTOFILL_ACCESSIBILITY_SUGGESTION
            else -> AnyPage.AUTOFILL_DROPDOWN_SUGGESTION
        }

        
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = page
        )

        
        logRepository.queueEvent(
            AutofillSuggestAnonymous(
                vaultTypeList = listOf(itemType),
                domain = Domain(Sha256Hash.of(packageName), domainType),
                isNativeApp = isNativeApp
            )
        )
        logRepository.queueEvent(
            AutofillSuggest(
                webcardItemTotalCount = totalCount,
                vaultTypeList = listOf(itemType),
                isNativeApp = isNativeApp
            )
        )

        
        log(
            UsageLogCode96(
                action = UsageLogCode96.Action.SHOW_IMPALA,
                app = packageName,
                sender = getSenderUsage96(origin),
                hasCredentials = totalCount != 0,
                type = type
            )
        )
    }

    private fun onNoResults(origin: Int, packageName: String, type: UsageLogCode96.Type) {
        log(
            UsageLogCode96(
                action = UsageLogCode96.Action.HIDDEN_IMPALA,
                app = packageName,
                sender = getSenderUsage96(origin),
                hasCredentials = false,
                type = type
            )
        )
    }

    private fun onClickToAutoFillButLock(action: String, itemUrl: String?) {
        val lockType = SingletonProvider.getSessionManager().session?.let {
            SingletonProvider.getComponent()
                .lockRepository
                .getLockManager(it)
                .getLockType()
        }
        val lockTypeStr =
            when (lockType) {
                LockTypeManager.LOCK_TYPE_BIOMETRIC -> UsageLogConstant.LockType.fingerPrint
                LockTypeManager.LOCK_TYPE_MASTER_PASSWORD -> UsageLogConstant.LockType.master
                LockTypeManager.LOCK_TYPE_PIN_CODE -> UsageLogConstant.LockType.pin
                else -> "?"
            }

        log(
            UsageLogCode75(
                action = action,
                subaction = UsageLogConstant.LockSubAction.from3rdParty,
                type = UsageLogConstant.LockAction.lock,
                subtype = lockTypeStr,
                website = itemUrl
            )
        )
    }

    private fun onClickToAutoFillNotLock(
        @AutofillOrigin origin: Int,
        packageName: String,
        itemUrl: String?,
        type: UsageLogCode96.Type
    ) {
        log(
            UsageLogCode96(
                action = UsageLogCode96.Action.AUTOLOGIN,
                app = packageName,
                sender = getSenderUsage96(origin),
                website = itemUrl,
                hasCredentials = true,
                type = type
            )
        )
    }

    private fun sendInstallLog42(
        origin: Int,
        packageName: String,
        action: InstallLogCode42.Action
    ) {
        log(
            InstallLogCode42(
                sender = getSenderInstall42(origin),
                action = action,
                appPackage = packageName
            )
        )
    }

    private fun SupportedAutofills?.toUsageLog96Type(): UsageLogCode96.Type? {
        val supportedResponses = this ?: return null

        return when (supportedResponses) {
            SupportedAutofills.AUTHENTIFIANT -> UsageLogCode96.Type.AUTHENTICATION
            SupportedAutofills.CREDIT_CARD -> UsageLogCode96.Type.PAYMENT_MEAN_CREDITCARD
            SupportedAutofills.EMAIL -> UsageLogCode96.Type.EMAIL
        }
    }

    companion object {
        fun getSenderUsage96(@AutofillOrigin origin: Int): UsageLogCode96.Sender? {
            return when (origin) {
                AUTO_FILL_API -> UsageLogCode96.Sender.AUTOFILL_API
                IN_APP_LOGIN -> UsageLogCode96.Sender.DASHLANE
                INLINE_AUTOFILL_KEYBOARD -> UsageLogCode96.Sender.AUTOFILL_API_KEYBOARD
                else -> null
            }
        }

        fun getSenderInstall42(@AutofillOrigin origin: Int): InstallLogCode42.Sender? {
            return when (origin) {
                AUTO_FILL_API -> InstallLogCode42.Sender.AUTOFILL_API
                IN_APP_LOGIN -> InstallLogCode42.Sender.DASHLANE
                INLINE_AUTOFILL_KEYBOARD -> InstallLogCode42.Sender.AUTOFILL_API_KEYBOARD
                else -> null
            }
        }
    }
}