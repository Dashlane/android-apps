package com.dashlane.autofill.core

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.AutofillOrigin.INLINE_AUTOFILL_KEYBOARD
import com.dashlane.autofill.AutofillOrigin.IN_APP_LOGIN
import com.dashlane.autofill.phishing.PhishingAttemptLevel
import com.dashlane.autofill.util.AutofillLogUtil
import com.dashlane.ext.application.TrustedBrowserApplication
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.AutofillButton
import com.dashlane.hermes.generated.definitions.AutofillMechanism
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.FieldsFilled
import com.dashlane.hermes.generated.definitions.FormType
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.hermes.generated.definitions.PhishingRisk
import com.dashlane.hermes.generated.events.anonymous.AutofillSuggestAnonymous
import com.dashlane.hermes.generated.events.anonymous.PerformAutofillAnonymous
import com.dashlane.hermes.generated.events.user.AutofillClick
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.PerformAutofill
import com.dashlane.url.UrlDomain
import javax.inject.Inject
import com.dashlane.hermes.generated.definitions.AutofillOrigin as HermesAutofillOrigin

@Suppress("LargeClass")
class AutofillUsageLog @Inject constructor(
    private val logRepository: LogRepository
) : AutofillAnalyzerDef.IAutofillUsageLog {

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
        totalCount: Int,
        phishingAttemptLevel: PhishingAttemptLevel
    ) {
        onShowList(
            origin = origin,
            packageName = packageName,
            itemType = ItemType.CREDENTIAL,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
            phishingAttemptLevel = phishingAttemptLevel
        )
    }

    override fun onShowCreditCardList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int,
        phishingAttemptLevel: PhishingAttemptLevel
    ) {
        onShowList(
            origin = origin,
            packageName = packageName,
            itemType = ItemType.CREDIT_CARD,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
            phishingAttemptLevel = phishingAttemptLevel
        )
    }

    override fun onShowEmailList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int,
        phishingAttemptLevel: PhishingAttemptLevel
    ) {
        onShowList(
            origin = origin,
            packageName = packageName,
            itemType = ItemType.CREDIT_CARD,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
            phishingAttemptLevel = phishingAttemptLevel,
        )
    }

    override fun onAutoFillCredentialDone(
        @AutofillOrigin origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        itemUrlDomain: UrlDomain?,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism,
        credentialId: String?,
    ) {
        logPerformAutofill(
            websiteUrlDomain = websiteUrlDomain,
            packageName = packageName,
            autofillOrigin = autofillOrigin,
            autofillMechanism = autofillMechanism,
            matchType = matchType,
            fieldsFilled = FieldsFilled(credential = 1),
            formType = FormType.LOGIN,
            credentialId = credentialId,
        )
    }

    private fun logPerformAutofill(
        websiteUrlDomain: UrlDomain?,
        packageName: String,
        autofillOrigin: com.dashlane.hermes.generated.definitions.AutofillOrigin,
        autofillMechanism: AutofillMechanism,
        matchType: MatchType,
        fieldsFilled: FieldsFilled,
        formType: FormType,
        credentialId: String?,
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
                mobileBrowserName = browser,
                credentialFilledItemId = credentialId?.let { ItemId(it) }
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
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism,
        credentialId: String,
    ) {
        logPerformAutofill(
            websiteUrlDomain = websiteUrlDomain,
            packageName = packageName,
            autofillOrigin = autofillOrigin,
            autofillMechanism = autofillMechanism,
            matchType = matchType,
            fieldsFilled = FieldsFilled(creditCard = 1),
            formType = FormType.PAYMENT,
            credentialId = credentialId,
        )
    }

    override fun onAutoFillEmailDone(
        origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism,
        credentialId: String,
    ) {
        logPerformAutofill(
            websiteUrlDomain = websiteUrlDomain,
            packageName = packageName,
            autofillOrigin = autofillOrigin,
            autofillMechanism = autofillMechanism,
            matchType = matchType,
            fieldsFilled = FieldsFilled(email = 1),
            formType = FormType.LOGIN,
            credentialId = credentialId,
        )
    }

    override fun onAutoFillWarningClick() {
        logRepository.queueEvent(
            AutofillClick(
                autofillButton = AutofillButton.PHISHING_RISK,
            )
        )
    }

    private fun onShowList(
        @AutofillOrigin origin: Int,
        packageName: String,
        itemType: ItemType,
        isNativeApp: Boolean,
        totalCount: Int,
        phishingAttemptLevel: PhishingAttemptLevel
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
        val phishingRisk = when (phishingAttemptLevel) {
            PhishingAttemptLevel.NONE -> PhishingRisk.NONE
            PhishingAttemptLevel.MODERATE -> PhishingRisk.MODERATE
            PhishingAttemptLevel.HIGH -> PhishingRisk.HIGH
        }

        
        logRepository.queuePageView(
            component = BrowseComponent.OS_AUTOFILL,
            page = page
        )

        
        logRepository.queueEvent(
            AutofillSuggestAnonymous(
                vaultTypeList = listOf(itemType),
                domain = Domain(Sha256Hash.of(packageName), domainType),
                isNativeApp = isNativeApp,
                phishingRisk = phishingRisk,
            )
        )
        logRepository.queueEvent(
            AutofillSuggest(
                webcardItemTotalCount = totalCount,
                vaultTypeList = listOf(itemType),
                isNativeApp = isNativeApp,
                phishingRisk = phishingRisk,
            )
        )
    }
}