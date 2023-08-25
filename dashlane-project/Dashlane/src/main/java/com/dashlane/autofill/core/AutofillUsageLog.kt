package com.dashlane.autofill.core

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.AutofillOrigin.AUTO_FILL_API
import com.dashlane.autofill.AutofillOrigin.INLINE_AUTOFILL_KEYBOARD
import com.dashlane.autofill.AutofillOrigin.IN_APP_LOGIN
import com.dashlane.autofill.api.fillresponse.relatedOnlyByLinkedDomains
import com.dashlane.autofill.api.ui.AutofillFeature
import com.dashlane.autofill.api.util.AutofillLogUtil
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ext.application.TrustedBrowserApplication
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.BrowseComponent
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
import com.dashlane.useractivity.log.usage.UsageLogCode5
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject
import com.dashlane.hermes.generated.definitions.AutofillOrigin as HermesAutofillOrigin

@Suppress("LargeClass")
class AutofillUsageLog @Inject constructor(
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val logRepository: LogRepository
) : AutofillAnalyzerDef.IAutofillUsageLog, AutofillLegacyLogger(
    sessionManager,
    bySessionUsageLogRepository
) {

    private var lastLoginFormFoundPackage: String? = null

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
            origin = origin,
            packageName = packageName,
            itemType = ItemType.CREDIT_CARD,
            isNativeApp = isNativeApp,
            totalCount = totalCount
        )
    }

    override fun onClickToAutoFillCredentialButLock(@AutofillOrigin origin: Int, itemUrl: String?) {
        onClickToAutoFillButLock(UsageLogConstant.LockAction.unlock, itemUrl)
    }

    override fun onClickToAutoFillCreditCardButLock(@AutofillOrigin origin: Int) {
        onClickToAutoFillButLock(UsageLogConstant.LockAction.unlockCreditCard, null)
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
        val browser = packageName.takeIf {
            TrustedBrowserApplication.getAppForPackage(it) != null
        }
        logRepository.queueEvent(
            PerformAutofill(
                isAutologin = false,
                autofillOrigin = autofillOrigin,
                autofillMechanism = autofillMechanism,
                isManual = false,
                matchType = matchType,
                fieldsFilled = fieldsFilled,
                formTypeList = listOf(formType),
                mobileBrowserName = browser
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
}