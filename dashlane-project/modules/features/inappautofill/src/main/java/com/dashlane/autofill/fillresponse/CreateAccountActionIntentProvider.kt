package com.dashlane.autofill.fillresponse

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

interface CreateAccountActionIntentProvider {
    fun getCreateAccountIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean,
        forKeyboard: Boolean,
        isAccountFrozen: Boolean
    ): IntentSender
}