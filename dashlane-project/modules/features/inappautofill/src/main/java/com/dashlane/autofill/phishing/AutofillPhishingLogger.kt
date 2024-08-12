package com.dashlane.autofill.phishing

interface AutofillPhishingLogger {

    fun logSettingChanged(isEnabled: Boolean)

    fun onSuggestAutoFillRiskToNone(isNativeApp: Boolean, packageName: String)

    fun onAcceptAutoFillRisk(
        isNativeApp: Boolean,
        packageName: String,
        phishingAttemptLevel: PhishingAttemptLevel,
    )

    fun onDismissAutoFill(
        isNativeApp: Boolean,
        packageName: String,
        trust: Boolean
    )
}