package com.dashlane.autofill.rememberaccount.model

import com.dashlane.autofill.formdetector.model.AutoFillFormSource

data class RememberedFormSource(
    val autoFillFormSource: AutoFillFormSource,
    val authentifiantId: String
)