package com.dashlane.autofill.api.fillresponse

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary



interface ViewAllAccountsActionIntentProvider {
    fun getViewAllAccountsIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean,
        forKeyboard: Boolean
    ): IntentSender
}