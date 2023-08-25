package com.dashlane.autofill.api.changepassword

import android.content.Context
import com.dashlane.autofill.api.changepassword.view.AutofillChangePasswordActivity
import com.dashlane.autofill.api.fillresponse.ChangePasswordActionIntentProvider
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import javax.inject.Inject

class AutofillChangePasswordActivityIntentProvider @Inject constructor() : ChangePasswordActionIntentProvider {

    override fun getChangePasswordIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        forKeyboard: Boolean
    ) = AutofillChangePasswordActivity.getAuthIntentSenderForChangePassword(context, summary, forKeyboard)
}