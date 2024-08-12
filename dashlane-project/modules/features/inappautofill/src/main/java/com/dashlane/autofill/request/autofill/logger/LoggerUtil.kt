package com.dashlane.autofill.request.autofill.logger

import androidx.annotation.CheckResult
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.AutofillOrigin
import com.dashlane.autofill.formdetector.AutoFillFormType
import com.dashlane.autofill.phishing.PhishingAttemptLevel
import com.dashlane.hermes.generated.definitions.AutofillOrigin as HermesAutofillOrigin

internal fun AutofillAnalyzerDef.IAutofillUsageLog.logShowList(
    packageName: String,
    @AutoFillFormType.FormType formType: Int,
    forKeyboard: Boolean,
    isNativeApp: Boolean,
    totalCount: Int,
    phishingAttemptLevel: PhishingAttemptLevel
) {
    when (formType) {
        AutoFillFormType.CREDIT_CARD -> onShowCreditCardList(
            origin = getAutofillApiOrigin(forKeyboard),
            packageName = packageName,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
            phishingAttemptLevel = phishingAttemptLevel,
        )
        AutoFillFormType.CREDENTIAL,
        AutoFillFormType.USERNAME_ONLY,
        AutoFillFormType.USERNAME_OR_EMAIL -> onShowCredentialsList(
            origin = getAutofillApiOrigin(forKeyboard),
            packageName = packageName,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
            phishingAttemptLevel = phishingAttemptLevel,
        )
        AutoFillFormType.EMAIL_ONLY -> onShowEmailList(
            origin = getAutofillApiOrigin(forKeyboard),
            packageName = packageName,
            isNativeApp = isNativeApp,
            totalCount = totalCount,
            phishingAttemptLevel = phishingAttemptLevel,
        )
        else -> Unit
    }
}

@AutofillOrigin
@CheckResult
internal fun getAutofillApiOrigin(isKeyboard: Boolean): Int = if (isKeyboard) {
    AutofillOrigin.INLINE_AUTOFILL_KEYBOARD
} else {
    AutofillOrigin.AUTO_FILL_API
}

@CheckResult
internal fun getHermesAutofillApiOrigin(isKeyboard: Boolean): HermesAutofillOrigin = if (isKeyboard) {
    HermesAutofillOrigin.KEYBOARD
} else {
    HermesAutofillOrigin.DROPDOWN
}