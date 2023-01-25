package com.dashlane.autofill.api.internal

import com.dashlane.autofill.formdetector.model.AutoFillFormSource



interface AutofillLimiter {
    suspend fun canHandle(formSource: AutoFillFormSource): Boolean
}