package com.dashlane.autofill.viewallaccounts

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.fillresponse.ViewAllAccountsActionIntentProvider
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.frozenautofill.FrozenAutofillActivity
import com.dashlane.autofill.phishing.PhishingAttemptLevel
import com.dashlane.autofill.viewallaccounts.view.AutofillViewAllItemsActivity
import javax.inject.Inject

class AutofillViewAllItemsActivityIntentProvider @Inject constructor() : ViewAllAccountsActionIntentProvider {
    override fun getViewAllAccountsIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean,
        forKeyboard: Boolean,
        phishingAttemptLevel: PhishingAttemptLevel,
        isAccountFrozen: Boolean,
    ): IntentSender {
        if (isAccountFrozen) {
            return FrozenAutofillActivity.getPendingIntent(context, summary)
        }

        return AutofillViewAllItemsActivity.getAuthIntentSenderForViewAllItems(
            context,
            summary,
            hadCredentials,
            forKeyboard,
            phishingAttemptLevel
        )
    }
}