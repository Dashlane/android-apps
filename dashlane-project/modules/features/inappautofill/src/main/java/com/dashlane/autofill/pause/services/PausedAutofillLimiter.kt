package com.dashlane.autofill.pause.services

import com.dashlane.autofill.internal.AutofillLimiter
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import javax.inject.Inject

class PausedAutofillLimiter @Inject constructor(private val pausedFormSourcesProvider: PausedFormSourcesProvider) :
    AutofillLimiter {

    override suspend fun canHandle(formSource: AutoFillFormSource): Boolean {
        return pausedFormSourcesProvider.isPaused(formSource)
    }
}