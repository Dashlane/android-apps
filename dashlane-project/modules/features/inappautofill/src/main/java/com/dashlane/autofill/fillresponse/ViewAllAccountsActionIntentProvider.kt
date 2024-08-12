package com.dashlane.autofill.fillresponse

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.phishing.PhishingAttemptLevel

interface ViewAllAccountsActionIntentProvider {
    fun getViewAllAccountsIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean,
        forKeyboard: Boolean,
        phishingAttemptLevel: PhishingAttemptLevel,
        isAccountFrozen: Boolean,
    ): IntentSender
}