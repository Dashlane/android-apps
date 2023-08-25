package com.dashlane.autofill.api.createaccount

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.api.createaccount.view.AutofillCreateAccountActivity
import com.dashlane.autofill.api.fillresponse.CreateAccountActionIntentProvider
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import javax.inject.Inject

class AutofillCreateAccountActivityIntentProvider @Inject constructor() : CreateAccountActionIntentProvider {
    override fun getCreateAccountIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean,
        forKeyboard: Boolean
    ): IntentSender {
        return AutofillCreateAccountActivity.getAuthIntentSenderForCreateAccount(
            context,
            summary,
            hadCredentials,
            forKeyboard
        )
    }
}