package com.dashlane.autofill.fillresponse

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.phishing.PhishingAttemptLevel

interface PhishingWarningIntentProvider {
    fun getPhishingIntentAction(
        context: Context,
        summary: AutoFillHintSummary,
        phishingAttemptLevel: PhishingAttemptLevel,
        isAccountFrozen: Boolean
    ): IntentSender
}