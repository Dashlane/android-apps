package com.dashlane.autofill.createaccount

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.createaccount.view.AutofillCreateAccountActivity
import com.dashlane.autofill.fillresponse.CreateAccountActionIntentProvider
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.frozenautofill.FrozenAutofillActivity
import javax.inject.Inject

class AutofillCreateAccountActivityIntentProvider @Inject constructor() : CreateAccountActionIntentProvider {
    override fun getCreateAccountIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean,
        forKeyboard: Boolean,
        isAccountFrozen: Boolean,
    ): IntentSender {
        if (isAccountFrozen) {
            return FrozenAutofillActivity.getPendingIntent(context, summary)
        }

        return AutofillCreateAccountActivity.getAuthIntentSenderForCreateAccount(
            context,
            summary,
            hadCredentials,
            forKeyboard
        )
    }
}