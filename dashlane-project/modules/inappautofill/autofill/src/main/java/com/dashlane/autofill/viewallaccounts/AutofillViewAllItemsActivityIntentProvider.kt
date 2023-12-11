package com.dashlane.autofill.viewallaccounts

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.fillresponse.ViewAllAccountsActionIntentProvider
import com.dashlane.autofill.viewallaccounts.view.AutofillViewAllItemsActivity
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import javax.inject.Inject

class AutofillViewAllItemsActivityIntentProvider @Inject constructor() : ViewAllAccountsActionIntentProvider {
    override fun getViewAllAccountsIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean,
        forKeyboard: Boolean
    ): IntentSender {
        return AutofillViewAllItemsActivity.getAuthIntentSenderForViewAllItems(
            context,
            summary,
            hadCredentials,
            forKeyboard
        )
    }
}