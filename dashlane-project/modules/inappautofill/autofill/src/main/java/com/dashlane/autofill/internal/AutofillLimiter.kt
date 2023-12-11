package com.dashlane.autofill.internal

import com.dashlane.autofill.formdetector.model.AutoFillFormSource

interface AutofillLimiter {
    suspend fun canHandle(formSource: AutoFillFormSource): Boolean
}