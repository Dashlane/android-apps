package com.dashlane.autofill.api

import android.content.Context
import com.dashlane.autofill.api.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.ext.application.ExternalApplication
import com.dashlane.url.toUrlOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AutofillFormSourcesStringsFromContext @Inject constructor(
    @ApplicationContext
    private val context: Context
) : AutofillFormSourcesStrings {
    override val applicationTypeString =
        context.getString(R.string.autofill_form_source_application)
    override val websiteTypeString =
        context.getString(R.string.autofill_form_source_website)

    override fun getApplicationString(applicationFormSource: ApplicationFormSource): String {
        return ExternalApplication.of(context, applicationFormSource.packageName)?.title
            ?: applicationFormSource.packageName
    }

    override fun getWebDomainString(webDomainFormSource: WebDomainFormSource): String {
        val webDomain = webDomainFormSource.webDomain
        return webDomain.toUrlOrNull()?.topPrivateDomain() ?: webDomain
    }
}
