package com.dashlane.autofill.api.pause.services

import com.dashlane.autofill.api.internal.AutofillLimiter
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import javax.inject.Inject



class PausedAutofillLimiter @Inject constructor(private val pausedFormSourcesProvider: PausedFormSourcesProvider) :
    AutofillLimiter {

    override suspend fun canHandle(formSource: AutoFillFormSource): Boolean {
        return pausedFormSourcesProvider.isPaused(formSource)
    }
}