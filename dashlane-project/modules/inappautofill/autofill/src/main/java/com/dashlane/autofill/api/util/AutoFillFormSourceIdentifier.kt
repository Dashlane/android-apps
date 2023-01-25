package com.dashlane.autofill.api.util

import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource

val AutoFillFormSource.formSourceIdentifier: String
    get() = when (this) {
        is ApplicationFormSource -> this.packageName
        is WebDomainFormSource -> this.webDomain
    }