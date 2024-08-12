package com.dashlane.autofill.changepassword

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.changepassword.view.AutofillChangePasswordActivity
import com.dashlane.autofill.fillresponse.ChangePasswordActionIntentProvider
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.frozenautofill.FrozenAutofillActivity
import javax.inject.Inject

class AutofillChangePasswordActivityIntentProvider @Inject constructor() : ChangePasswordActionIntentProvider {

    override fun getChangePasswordIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        forKeyboard: Boolean,
        isAccountFrozen: Boolean
    ): IntentSender {
        if (isAccountFrozen) {
            return FrozenAutofillActivity.getPendingIntent(context, summary)
        }
        return AutofillChangePasswordActivity.getAuthIntentSenderForChangePassword(context, summary, forKeyboard)
    }
}