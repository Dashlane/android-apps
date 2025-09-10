package com.dashlane.autofill.logs

import androidx.annotation.CheckResult
import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.AutofillOrigin.AUTO_FILL_API
import com.dashlane.autofill.AutofillOrigin.INLINE_AUTOFILL_KEYBOARD
import com.dashlane.autofill.AutofillOrigin.IN_APP_LOGIN
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.model.AuthentifiantItemToFill
import com.dashlane.autofill.model.CreditCardItemToFill
import com.dashlane.autofill.util.AutofillLogUtil
import com.dashlane.ext.application.TrustedBrowserApplication
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.activityLog
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
import com.dashlane.hermes.generated.events.anonymous.AutofillSuggestAnonymous
import com.dashlane.hermes.generated.events.anonymous.PerformAutofillAnonymous
import com.dashlane.hermes.generated.events.user.AutofillClick
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import com.dashlane.hermes.generated.events.user.PerformAutofill
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.Properties
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.url.UrlDomain
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.isSemanticallyNull
import javax.inject.Inject
import com.dashlane.hermes.generated.definitions.AutofillOrigin as HermesAutofillOrigin

interface AutofillUsageLog {
    fun onLoginFormFound(packageName: String)

    fun onShowCredentialsList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int,
    )

    fun onShowCreditCardList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int,
    )

    fun onShowEmailList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int,
    )

    fun onAutoFillCredentialDone(
        itemToFill: AuthentifiantItemToFill,
        @AutofillOrigin origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism,
    )

    fun onAutoFillCreditCardDone(
        itemToFill: CreditCardItemToFill,
        @AutofillOrigin origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism,
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

    fun logShowList(
        packageName: String,
        @AutoFillFormType.FormType formType: Int,
        forKeyboard: Boolean,
        isNativeApp: Boolean,
        totalCount: Int,
    )
}

@Suppress("LargeClass")
class AutofillUsageLogImpl @Inject constructor(
    private val logRepository: LogRepository,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
) : AutofillUsageLog {
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
    ) {
        onShowList(
            origin = origin,
            packageName = packageName,
            itemType = ItemType.CREDENTIAL,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
        )
    }

    override fun onShowCreditCardList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int,
    ) {
        onShowList(
            origin = origin,
            packageName = packageName,
            itemType = ItemType.CREDIT_CARD,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
        )
    }

    override fun onShowEmailList(
        @AutofillOrigin origin: Int,
        packageName: String,
        isNativeApp: Boolean,
        totalCount: Int,
    ) {
        onShowList(
            origin = origin,
            packageName = packageName,
            itemType = ItemType.CREDIT_CARD,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
        )
    }

    override fun onAutoFillCredentialDone(
        itemToFill: AuthentifiantItemToFill,
        @AutofillOrigin origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism,
    ) {
        sendActivityLog(
            spaceId = itemToFill.spaceId,
            logType = ActivityLog.LogType.USER_PERFORMED_AUTOFILL_CREDENTIAL,
            properties = Properties(
                credentialDomain = itemToFill.url.toUrlDomainOrNull().toString(),
                credentialLogin = itemToFill.login,
                autoFilledDomain = websiteUrlDomain.toString(),
            ),
        )
        logPerformAutofill(
            websiteUrlDomain = websiteUrlDomain,
            packageName = packageName,
            autofillOrigin = autofillOrigin,
            autofillMechanism = autofillMechanism,
            matchType = matchType,
            fieldsFilled = FieldsFilled(credential = 1),
            formType = FormType.LOGIN,
            credentialId = itemToFill.itemId,
        )
    }

    private fun logPerformAutofill(
        websiteUrlDomain: UrlDomain?,
        packageName: String,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism,
        matchType: MatchType,
        fieldsFilled: FieldsFilled,
        formType: FormType,
        credentialId: String?,
    ) {
        val domainWrapper = AutofillLogUtil.extractDomainFrom(
            urlDomain = websiteUrlDomain,
            packageName = packageName,
        )
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
                credentialFilledItemId = credentialId?.let { ItemId(it) },
            ),
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
                formTypeList = listOf(formType),
            ),
        )
    }

    override fun onAutoFillCreditCardDone(
        itemToFill: CreditCardItemToFill,
        @AutofillOrigin origin: Int,
        packageName: String,
        websiteUrlDomain: UrlDomain?,
        matchType: MatchType,
        autofillOrigin: HermesAutofillOrigin,
        autofillMechanism: AutofillMechanism,
    ) {
        sendActivityLog(
            spaceId = itemToFill.spaceId,
            logType = ActivityLog.LogType.USER_PERFORMED_AUTOFILL_PAYMENT,
            properties = Properties(
                itemName = itemToFill.name,
                itemType = Properties.ItemType.CREDIT_CARD,
                autoFilledDomain = websiteUrlDomain.toString(),
            ),
        )
        logPerformAutofill(
            websiteUrlDomain = websiteUrlDomain,
            packageName = packageName,
            autofillOrigin = autofillOrigin,
            autofillMechanism = autofillMechanism,
            matchType = matchType,
            fieldsFilled = FieldsFilled(creditCard = 1),
            formType = FormType.PAYMENT,
            credentialId = itemToFill.itemId,
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
        logRepository.queueEvent(AutofillClick(autofillButton = AutofillButton.PHISHING_RISK))
    }

    override fun logShowList(
        packageName: String,
        @AutoFillFormType.FormType formType: Int,
        forKeyboard: Boolean,
        isNativeApp: Boolean,
        totalCount: Int,
    ) {
        when (formType) {
            AutoFillFormType.CREDIT_CARD -> onShowCreditCardList(
                origin = getAutofillApiOrigin(forKeyboard),
                packageName = packageName,
                isNativeApp = isNativeApp,
                totalCount = totalCount,
            )
            AutoFillFormType.CREDENTIAL,
            AutoFillFormType.USERNAME_ONLY,
            AutoFillFormType.USERNAME_OR_EMAIL,
            -> onShowCredentialsList(
                origin = getAutofillApiOrigin(forKeyboard),
                packageName = packageName,
                isNativeApp = isNativeApp,
                totalCount = totalCount,
            )
            AutoFillFormType.EMAIL_ONLY -> onShowEmailList(
                origin = getAutofillApiOrigin(forKeyboard),
                packageName = packageName,
                isNativeApp = isNativeApp,
                totalCount = totalCount,
            )
            else -> Unit
        }
    }

    private fun onShowList(
        @AutofillOrigin origin: Int,
        packageName: String,
        itemType: ItemType,
        isNativeApp: Boolean,
        totalCount: Int,
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
            page = page,
        )

        
        logRepository.queueEvent(
            AutofillSuggestAnonymous(
                vaultTypeList = listOf(itemType),
                domain = Domain(Sha256Hash.of(packageName), domainType),
                isNativeApp = isNativeApp,
            ),
        )
        logRepository.queueEvent(
            AutofillSuggest(
                webcardItemTotalCount = totalCount,
                vaultTypeList = listOf(itemType),
                isNativeApp = isNativeApp,
            ),
        )
    }

    private fun sendActivityLog(
        spaceId: String? = null,
        logType: ActivityLog.LogType,
        properties: Properties,
    ) {
        val teamSpaceAccessor = teamSpaceAccessorProvider.get() ?: return

        if (spaceId.isSemanticallyNull() ||
            teamSpaceAccessor.get(spaceId)?.isCollectSensitiveDataActivityLogsEnabled ?: false
        ) {
            return
        }

        val log = activityLog(
            logType = logType,
            properties = properties,
        )
        logRepository.queueEvent(log)
    }
}

@AutofillOrigin
@CheckResult
internal fun getAutofillApiOrigin(isKeyboard: Boolean): Int = if (isKeyboard) {
    INLINE_AUTOFILL_KEYBOARD
} else {
    AUTO_FILL_API
}

@CheckResult
internal fun getHermesAutofillApiOrigin(isKeyboard: Boolean): HermesAutofillOrigin = if (isKeyboard) {
    HermesAutofillOrigin.KEYBOARD
} else {
    HermesAutofillOrigin.DROPDOWN
}