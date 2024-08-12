package com.dashlane.autofill.fillresponse

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

interface ChangePasswordActionIntentProvider {
    fun getChangePasswordIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        forKeyboard: Boolean,
        isAccountFrozen: Boolean
    ): IntentSender
}