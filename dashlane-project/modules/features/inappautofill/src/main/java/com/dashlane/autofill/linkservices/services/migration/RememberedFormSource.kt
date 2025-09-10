package com.dashlane.autofill.linkservices.services.migration

import com.dashlane.autofill.formdetector.model.AutoFillFormSource

data class RememberedFormSource(
    val autoFillFormSource: AutoFillFormSource,
    val authentifiantId: String,
)