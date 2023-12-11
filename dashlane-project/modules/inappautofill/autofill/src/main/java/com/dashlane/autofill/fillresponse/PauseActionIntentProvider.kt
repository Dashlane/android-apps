package com.dashlane.autofill.fillresponse

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary

interface PauseActionIntentProvider {
    fun getPauseIntentSender(context: Context, summary: AutoFillHintSummary, hadCredentials: Boolean): IntentSender
}