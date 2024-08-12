package com.dashlane.autofill.phishing

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.fillresponse.PhishingWarningIntentProvider
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.frozenautofill.FrozenAutofillActivity
import javax.inject.Inject

class AutofillPhishingActivityIntentProvider @Inject constructor() : PhishingWarningIntentProvider {
    override fun getPhishingIntentAction(
        context: Context,
        summary: AutoFillHintSummary,
        phishingAttemptLevel: PhishingAttemptLevel,
        isAccountFrozen: Boolean
    ): IntentSender {
        if (isAccountFrozen) {
            return FrozenAutofillActivity.getPendingIntent(context, summary)
        }

        return PhishingWarningActivity.getAuthIntentSenderForPhishingWarning(context, summary, phishingAttemptLevel)
    }
}