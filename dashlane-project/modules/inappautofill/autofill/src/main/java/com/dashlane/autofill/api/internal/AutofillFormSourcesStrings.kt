package com.dashlane.autofill.api.internal

import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource

interface AutofillFormSourcesStrings {
    val applicationTypeString: String
    val websiteTypeString: String

    fun getApplicationString(applicationFormSource: ApplicationFormSource): String
    fun getWebDomainString(webDomainFormSource: WebDomainFormSource): String

    fun getAutoFillFormSourceString(autoFillFormSource: AutoFillFormSource): String {
        return when (autoFillFormSource) {
            is ApplicationFormSource -> getApplicationString(autoFillFormSource)
            is WebDomainFormSource -> getWebDomainString(autoFillFormSource)
        }
    }

    fun getAutoFillFormSourceTypeString(autoFillFormSource: AutoFillFormSource): String {
        return when (autoFillFormSource) {
            is ApplicationFormSource -> applicationTypeString
            is WebDomainFormSource -> websiteTypeString
        }
    }
}
