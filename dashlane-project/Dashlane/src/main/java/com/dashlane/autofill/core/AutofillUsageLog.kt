package com.dashlane.autofill.core

import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.AutofillOrigin.INLINE_AUTOFILL_KEYBOARD
import com.dashlane.autofill.AutofillOrigin.IN_APP_LOGIN
import com.dashlane.autofill.ui.AutofillFeature
import com.dashlane.autofill.util.AutofillLogUtil
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
}