package com.dashlane.autofill.api.ui

import com.dashlane.autofill.api.FillRequestHandler
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch



internal class FillSuggestionToActivityResult(
    private val fillRequestHandler: FillRequestHandler,
    private val coroutineScope: CoroutineScope
) {

    fun finishWithRequestResult(activity: AutoFillResponseActivity, summary: AutoFillHintSummary) {
        coroutineScope.launch(Main) {
            val response = fillRequestHandler.getFillResponse(activity, null, summary, null, null)
            if (response == null) {
                activity.finish()
            } else {
                activity.finishWithResultIntentResult(response)
            }
        }
    }
}