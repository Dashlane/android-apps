package com.dashlane.autofill.api.fillresponse

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary



interface CreateAccountActionIntentProvider {
    fun getCreateAccountIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean,
        forKeyboard: Boolean
    ): IntentSender
}