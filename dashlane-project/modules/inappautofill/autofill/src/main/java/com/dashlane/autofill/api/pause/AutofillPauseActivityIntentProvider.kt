package com.dashlane.autofill.api.pause

import android.content.Context
import android.content.IntentSender
import com.dashlane.autofill.api.fillresponse.PauseActionIntentProvider
import com.dashlane.autofill.api.pause.view.AutofillPauseActivity
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import javax.inject.Inject



class AutofillPauseActivityIntentProvider @Inject constructor() : PauseActionIntentProvider {

    override fun getPauseIntentSender(
        context: Context,
        summary: AutoFillHintSummary,
        hadCredentials: Boolean
    ): IntentSender {
        return AutofillPauseActivity.getAuthIntentSenderForPause(
            context,
            summary,
            hadCredentials
        )
    }
}